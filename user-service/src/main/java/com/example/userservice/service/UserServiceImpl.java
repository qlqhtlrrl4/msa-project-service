package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
//@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
//    private final RestTemplate restTemplate;
    private final Environment env;
    private final OrderServiceClient orderServiceClient;

    private final CircuitBreakerFactory circuitBreakerFactory;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                           Environment env, OrderServiceClient orderServiceClient,
                           CircuitBreakerFactory circuitBreakerFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
        this.orderServiceClient = orderServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<UserEntity> userEntity = userRepository.findByEmail(email);

        if(userEntity.isEmpty()) {
            throw new UsernameNotFoundException(email);
        }

        return new User(userEntity.get().getEmail(), userEntity.get().getEncryptedPwd(),
                true, true, true, true
                // 권한 때문에 List 로
                ,new ArrayList<>());
    }

    @Override
    public UserDto createUser(UserDto userDto) {

        userDto.setUserId(UUID.randomUUID().toString());

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity  = mapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

        userRepository.save(userEntity);

        return mapper.map(userEntity, UserDto.class);
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        Optional<UserEntity> result  = userRepository.findByUserId(userId);

        if(result.isEmpty()) {
            throw new UsernameNotFoundException("User not Found");
        }

        UserDto userDto = new ModelMapper().map(result.get(), UserDto.class);
//        List<ResponseOrder> orders = new ArrayList<>();

        /**
         * 1. using as RestTemplate
         */
//        String orderUrl = String.format(env.getProperty("order-service.url"), userId);
//
//        ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(
//                orderUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<ResponseOrder>>() {
//                });
//
//        userDto.setOrders(orderListResponse.getBody());

        /**
         * 2. using openfeign
         */
        //Feign Exception Handling
//        List<ResponseOrder> orders = null;
//        try {
//            orders = orderServiceClient.getOrders(userId);
//        } catch (FeignException e) {
//            log.error(e.getMessage());
//        }
//
//        userDto.setOrders(orders);

        /**
         * 3. errorDecoder 사용 try - catch 제거
         */
        //List<ResponseOrder> orders = orderServiceClient.getOrders(userId);
        log.info("Before call orders microservice");
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");

        List<ResponseOrder> orders = circuitbreaker.run(() -> orderServiceClient.getOrders(userId),
                throwable ->  new ArrayList<>());

        log.info("After call orders microservice");
        userDto.setOrders(orders);

        return userDto;
    }

    @Override
    public List<UserDto> getUserByAll() {
        List<UserEntity> result = userRepository.findAll();
        List<UserDto> convert = new ArrayList<>();

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        result.forEach(ue -> {
            convert.add(mapper.map(ue, UserDto.class));
        });

        return convert;
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);

        if(userEntity.isEmpty()) {
            throw new UsernameNotFoundException("not search user");
        }

        return new ModelMapper().map(userEntity.get(), UserDto.class);

    }
}
