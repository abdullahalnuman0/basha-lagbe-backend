package com.massseat.app.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_settings")
public class AppSetting extends BaseEntity{

    @Column(name = "setting_key", nullable = false,unique = true,length = 80)
    private String key;

    @Column(name = "setting_value", length = 2000)
    private String value;

}
