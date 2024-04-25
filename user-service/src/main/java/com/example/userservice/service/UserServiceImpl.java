package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
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
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
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
        List<ResponseOrder> orders = new ArrayList<>();
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
