package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
//@RequestMapping("/user-service")
@RequiredArgsConstructor
public class UserController {
    //case 1) env 사용
    private final Environment env;
    private final Greeting greeting;
    private final UserService userservice;

    @GetMapping("/health_check")
    public String status() {

        return "It's Working in User Service , "
                +"port(local.server.port) = " +env.getProperty("local.server.port")
                +"port(server.port) = " +env.getProperty("server.port")
                +"token secret = " +env.getProperty("token.secret")
                +"token expiration time  = " +env.getProperty("token.expiration_time");

    }

    @GetMapping("/welcome")
    public String welcome() {

        //return env.getProperty("greeting.message");
        return greeting.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody RequestUser user) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        UserDto userDto = mapper.map(user, UserDto.class);
        userservice.createUser(userDto);

        ResponseUser responseUser = mapper.map(userDto, ResponseUser.class);

        return new ResponseEntity<>(responseUser,HttpStatus.CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {

        List<UserDto> users = userservice.getUserByAll();
        List<ResponseUser> result = new ArrayList<>();

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        users.forEach( u -> {
            result.add(mapper.map(u, ResponseUser.class));
        });

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable("userId")String userId) {

        UserDto findUser = userservice.getUserByUserId(userId);

        ResponseUser user = new ModelMapper().map(findUser, ResponseUser.class);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

}
