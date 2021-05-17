package com.game.service;

import com.game.entity.Player;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Optional<Player> findById(long id) {
        return playerRepository.findById(id);
    }

    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    public Player createPlayer(Player player) {
        return playerRepository.saveAndFlush(player);
    }

    public void deleteById(long id) {
        playerRepository.deleteById(id);
    }
}
