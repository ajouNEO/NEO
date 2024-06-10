package com.neo.back.mainService.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserBanServerListDto {
    String name;
    String source;
    String time;
    String expires;
    String reason;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserBanServerListDto that = (UserBanServerListDto) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(source, that.source) &&
               Objects.equals(time, that.time) &&
               Objects.equals(expires, that.expires) &&
               Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, source, time, expires, reason);
    }

}
