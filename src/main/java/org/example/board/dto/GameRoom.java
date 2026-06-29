package org.example.board.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.*;

@Data
public class GameRoom {
    private String roomId;
    private String hostUsername;
    private Map<String, RoomPlayer> players = new LinkedHashMap<>();
    private boolean started = false;

    private List<String> turnOrder = new ArrayList<>();
    private int currentTurnIndex = 0;
    private String lastWord = "";
    private List<String> usedWords = new ArrayList<>();
    private LocalDateTime turnStartTime; // 추가

    public GameRoom(String roomId, String hostUsername) {
        this.roomId = roomId;
        this.hostUsername = hostUsername;
    }

    public void addPlayer(String username) {
        players.put(username, new RoomPlayer(username, 0, 0, 100, true, "RIGHT", false));
    }

    public void removePlayer(String username) {
        players.remove(username);
        turnOrder.remove(username);
    }

    public boolean isFull() {
        return players.size() >= 4;
    }

    public String getCurrentTurnUsername() {
        if (turnOrder.isEmpty()) return null;
        return turnOrder.get(currentTurnIndex % turnOrder.size());
    }

    public void nextTurn() {
        currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
        turnStartTime = LocalDateTime.now(); // 턴 넘어갈 때마다 시간 갱신
    }

    public void eliminatePlayer(String username) {
        RoomPlayer player = players.get(username);
        if (player != null) player.setAlive(false);
        turnOrder.remove(username);
        if (currentTurnIndex >= turnOrder.size()) {
            currentTurnIndex = 0;
        }
        turnStartTime = LocalDateTime.now(); // 탈락 후 다음 턴 시간 갱신
    }

    public List<RoomPlayer> getAlivePlayers() {
        return players.values().stream()
                .filter(RoomPlayer::isAlive)
                .toList();
    }
}