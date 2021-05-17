package com.game.controller;

import com.game.entity.Player;
import com.game.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class PlayersController {
    private final PlayerService playerService;

    public PlayersController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @RequestMapping(value = "/rest/players", method = RequestMethod.GET)
    public ResponseEntity  allPlayers(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) String race,
            @RequestParam(value = "profession", required = false) String profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false)  Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(value = "order", required = false, defaultValue = "ID") PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {


        return ResponseEntity.ok(filteredList(
                name,
                title,
                race,
                profession,
                after,
                before,
                banned,
                minExperience,
                maxExperience,
                minLevel,
                maxLevel,
                order,
                pageNumber,
                pageSize,
                false));
    }

    @RequestMapping(value = "/rest/players/count", method = RequestMethod.GET)
    public ResponseEntity<Integer> playersCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) String race,
            @RequestParam(value = "profession", required = false) String profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false)  Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel) {


        return ResponseEntity.ok(filteredList(
                name,
                title,
                race,
                profession,
                after,
                before,
                banned,
                minExperience,
                maxExperience,
                minLevel,
                maxLevel,
                null,
                null,
                null,
                true).size());
    }

    @PostMapping("/rest/players/")
    public ResponseEntity createPlayer(@RequestBody Player player) {
        if (isBadParameters(player)) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        if (player.isBanned() == null) {
            player.setBanned(false);
        }
        Calendar date = Calendar.getInstance();
        date.setTime(player.getBirthday());
        if (date.get(Calendar.YEAR) < 2_000 || date.get(Calendar.YEAR) >= 3_000) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        return getPlayerResponseEntity(player);
    }

    @GetMapping("/rest/players/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable("id") Long id) {
        if (id <= 0) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Optional<Player> optionalPlayer = playerService.findById(id);
        if (!optionalPlayer.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        Player player = playerService.findById(id).get();
        return new ResponseEntity(player, HttpStatus.OK);
    }

    @PostMapping("/rest/players/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable("id") Long id, @RequestBody Player player) {
        if (id <= 0) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Optional<Player> optionalPlayer = playerService.findById(id);
        if (!optionalPlayer.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        Player responsePlayer = playerService.findById(id).get();
        if (player.getName() != null) {
            responsePlayer.setName(player.getName());
        }
        if (player.getTitle() != null) {
            responsePlayer.setTitle(player.getTitle());
        }
        if (player.getRace() != null) {
            responsePlayer.setRace(player.getRace());
        }
        if (player.getProfession() != null) {
            responsePlayer.setProfession(player.getProfession());
        }
        if (player.getBirthday() != null) {
            responsePlayer.setBirthday(player.getBirthday());
        }
        if (player.isBanned() != null) {
            responsePlayer.setBanned(player.isBanned());
        }
        if (player.getExperience() != null) {
            responsePlayer.setExperience(player.getExperience());
        }
        if (isBadParameters(responsePlayer)) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return getPlayerResponseEntity(responsePlayer);
    }

    @DeleteMapping("/rest/players/{id}")
    public ResponseEntity deletePlayer(@PathVariable("id") Long id) {
        if (id <= 0) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Optional<Player> optionalPlayer = playerService.findById(id);
        if (!optionalPlayer.isPresent()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        playerService.deleteById(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    private boolean isBadParameters(Player player) {
        if (
                player.getName() == null
                || player.getName().length() == 0
                || player.getName().length() > 12
                || player.getTitle() == null
                || player.getTitle().length() > 30
                || player.getRace() == null
                || player.getProfession() == null
                || player.getBirthday() == null
                || player.getBirthday().getTime() < 0
                || player.getExperience() == null
                || player.getExperience() < 0
                || player.getExperience() > 10000000) {
            return true;
        }
        return false;
    }


    private ResponseEntity getPlayerResponseEntity(@RequestBody Player player) {
        Double doubleLevel = ((Math.sqrt(2500 + 200 * player.getExperience()) - 50) / 100);
        int level = doubleLevel.intValue();
        player.setLevel(level);
        int untilNextLevel = 50 * (level + 1) * (level +2) - player.getExperience();
        player.setUntilNextLevel(untilNextLevel);
        Player responsePlayer = playerService.createPlayer(player);
        return new ResponseEntity(responsePlayer, HttpStatus.OK);
    }


    private List<Player> filteredList(
            String name,
            String title,
            String race,
            String profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel,
            PlayerOrder order,
            Integer pageNumber,
            Integer pageSize,
            Boolean isCountRequest) {
        List<Player> filteredPlayers = playerService.findAll();
        if (name != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(s -> s.getName().toLowerCase(Locale.ROOT).contains(name.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (title != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(s -> s.getTitle().toLowerCase(Locale.ROOT).contains(title.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }
        if (race != null) {
            filteredPlayers = filteredPlayers.stream().filter(s -> s.getRace().name().equals(race)).collect(Collectors.toList());
        }
        if (profession != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(s -> s.getProfession().name().equals(profession))
                    .collect(Collectors.toList());
        }
        if (after != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(s -> s.getBirthday().getTime() >= after)
                    .collect(Collectors.toList());
        }
        if (before != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(s -> s.getBirthday().getTime() <= before)
                    .collect(Collectors.toList());
        }
        if (banned != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(s -> s.isBanned() == banned)
                    .collect(Collectors.toList());
        }
        if (minExperience != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(s -> s.getExperience() >= minExperience)
                    .collect(Collectors.toList());
        }
        if (maxExperience != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(s -> s.getExperience() <= maxExperience)
                    .collect(Collectors.toList());
        }
        if (minLevel != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(s -> s.getLevel() >= minLevel)
                    .collect(Collectors.toList());
        }
        if (maxLevel != null) {
            filteredPlayers = filteredPlayers.stream()
                    .filter(s -> s.getLevel() <= maxLevel)
                    .collect(Collectors.toList());
        }
        if (isCountRequest) {
            return filteredPlayers;
        }
        switch (order) {
            case NAME:
                filteredPlayers.sort(Comparator.comparing(Player::getName));
                break;
            case EXPERIENCE:
                filteredPlayers.sort(Comparator.comparingInt(Player::getExperience));
                break;
            case BIRTHDAY:
                filteredPlayers.sort(Comparator.comparing(Player::getBirthday));
                break;
            default:
                filteredPlayers.sort(Comparator.comparing(Player::getId));
        }
        filteredPlayers = filteredPlayers.stream()
                .skip(pageNumber * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        return filteredPlayers;
    }

}
