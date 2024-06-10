package com.neo.back.mainService.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class MinecreftServerSetting extends GameServerSetting{

    private Boolean allow_flight = false;

    private Boolean allow_nether = true;

    private String difficulty = "easy";

    private Boolean enable_command_block = false;

    private Boolean enforce_whitelist = false;

    private String gamemode = "survival";

    private Boolean generate_structures = true;

    private Boolean hardcore = false;

    private Integer max_build_height = 256;

    private Integer max_players = 20;

    private String motd = "A Minecraft Server";

    private Boolean online_mode=true;

    private Integer player_idle_timeout=0;

    private Boolean prevent_proxy_connections = false;

    private Boolean pvp = true;

    private Boolean spawn_animals= true;

    private Boolean spawn_monsters = true;

    private Boolean spawn_npcs = true;

    private Integer spawn_protection = 16;

    private Integer view_distance = 10;

    private Boolean white_list = false;

}