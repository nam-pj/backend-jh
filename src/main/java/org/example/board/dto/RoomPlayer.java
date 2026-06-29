package org.example.board.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomPlayer {
    private String username;
    private double x;
    private double y;
    private int hp;
    private boolean alive;
    private String direction; // LEFT, RIGHT
    private boolean attacking;
}