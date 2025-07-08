/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.thesis.java.javalearning.controller.admin;

import com.thesis.java.javalearning.entity.User;

/**
 *
 * @author adewi
 */

public record UserDto(Long id, String fullName, String username, String role) {
  public static UserDto from(User u) {
    return new UserDto(
      u.getId(),
      u.getFullName(),
      u.getUsername(),
      u.getRole().name()
    );
  }
}