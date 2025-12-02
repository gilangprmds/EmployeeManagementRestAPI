package com.gcompany.employeemanagement.service;

import com.gcompany.employeemanagement.dto.Response;
import com.gcompany.employeemanagement.dto.req.UserRequest;
import com.gcompany.employeemanagement.dto.resp.UserResponse;
import com.gcompany.employeemanagement.enums.Role;
import com.gcompany.employeemanagement.model.User;
import com.gcompany.employeemanagement.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ResponseEntity<?> createUser(UserRequest userReq) {
        Response<Object> response = new Response<>();

        try {
            Optional<User> userDB = userRepository.findByEmail(userReq.getEmail());
            if (userDB.isPresent()) {
                response.setMessage("Email " + userReq.getEmail() + "  already exists");
                log.error("Email " + userReq.getEmail() + "  already exists");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
            User user = new User();
            user.setEmail(userReq.getEmail());
            user.setPassword(passwordEncoder.encode(userReq.getPassword()));
            user.setFullName(userReq.getFirstName().concat(" ").concat(userReq.getLastName()));
            user.setEmail(userReq.getEmail());
            user.setRole(Role.EMPLOYEE);
            userRepository.save(user);
            response.setMessage("User " + userReq.getEmail() + "  successfully created");
            log.info("User " + userReq.getEmail() + "  successfully created");
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }  catch (Exception e) {
            response.setMessage("Error " + e.getMessage());
            log.error("Error " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    public ResponseEntity<?> updateUser(Long id, UserRequest userReq) {
        Response<Object> response = new Response<>();
        try {
            Optional<User> userDB = userRepository.findById(id);
            if (userDB.isEmpty()) {
                response.setMessage("Email " + userReq.getEmail() + "  does not exist");
                log.error("Email " + userReq.getEmail() + "  does not exist");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }

            Optional<User> userExisting = userRepository.findByEmail(userReq.getEmail());
            if (userExisting.isPresent()) {
                response.setMessage("Email " + userReq.getEmail() + "  is already in use");
                log.error("Email " + userReq.getEmail() + "  is already in use");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
            User userUpdate = userDB.get();
            userUpdate.setEmail(userReq.getEmail());
            userUpdate.setPassword(passwordEncoder.encode(userReq.getPassword()));
            userRepository.save(userUpdate);
            response.setMessage("Email " + userReq.getEmail() + "  successfully updated");
            log.info("Email " + userReq.getEmail() + "  successfully updated");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        }catch (Exception e) {
            response.setMessage("Error " + e.getMessage());
            log.error("Error " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    public ResponseEntity<?> deleteUser(Long id) {
        Response<Object> response = new Response<>();
        try {
            Optional<User> userDB = userRepository.findById(id);
            if (userDB.isEmpty()) {
                response.setMessage("User with id " + id + " does not exist");
                log.error("User with id " + id + " does not exist");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
            userRepository.deleteById(id);
            response.setMessage("User with id " + id + " successfully deleted");
            log.info("User with id " + id + " successfully deleted");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            response.setMessage("Error " + e.getMessage());
            log.error("Error " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    public ResponseEntity<?> getAllUsers(Pageable pageable, String name, String role) {
        Response<Object> response = new Response<>();
        try {
            Specification<User> spec = Specification.where(null);

            if (name != null && !name.isBlank()) {
                spec = spec.and((root, query, cb) ->
                        cb.like(cb.lower(root.get("email")), "%" + name.toLowerCase() + "%")
                );
            }


//            if (role != null && !role.isBlank()) {
//                spec = spec.and((root, query, cb) ->
//                        cb.like(cb.lower(root.get("role").get("name")), "%" + role.toLowerCase() + "%")
//                );
//            }
            Role role1;
            if (role != null && !role.isBlank()) {
                try {
                    role1 = Role.valueOf(role.toUpperCase());
                } catch (IllegalArgumentException e) {
                    response.setMessage("User With Role " + role + " not found");
                    log.error("User With Role" + role + " not found");
                    return ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(response);
                }
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("role"),  role1)
                );
            }

            Page<User> usersPage = userRepository.findAll(spec, pageable);
            List<User> usersList = usersPage.getContent();
            if (usersList.isEmpty()) {
                response.setMessage("User not found");
                log.error("User  not found");
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }

            List<UserResponse> usersRespList = usersList.stream()
                    .map(user -> UserResponse.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .role(user.getRole().name())
                            .build()
                    )
                    .toList();

            Map<String, Object> result = new HashMap<>();
            result.put("users", usersRespList);
            result.put("currentPage", usersPage.getNumber());
            result.put("totalItems", usersPage.getTotalElements());
            result.put("totalPages", usersPage.getTotalPages());

            response.setMessage("Users retrieved successfully");
            response.setData(result);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            log.error("Error get users", e);
            response.setMessage("Internal server error: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    public ResponseEntity<?> getUserById(Long id) {
        Response<Object> response = new Response<>();
        try {
            Optional<User> userDB = userRepository.findById(id);
            if (userDB.isEmpty()) {
                response.setMessage("User id " + id + " does not exist");
                log.error("User id " + id + " does not exist");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);

            }
            User user = userDB.get();
            UserResponse userResponse = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .build();

            response.setMessage("User " + user.getEmail() + " With ID " + user.getId() + " successfully retrieved");
            log.info("User " + user.getEmail() + " With ID " + user.getId() + " successfully retrieved");
            return  ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(userResponse);
        }catch (Exception e) {
            response.setMessage("Error " + e.getMessage());
            log.error("Error " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }
}