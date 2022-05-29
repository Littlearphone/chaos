package com.littlearphone.fx.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Data
@Entity
// 等于这个值就会创建为这个类型
@DiscriminatorValue("smb")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SMBStorage extends BaseStorage {
    @Column(name = "username", nullable = false, columnDefinition = "varchar(64) default ''")
    private String username;
    @Column(name = "password", nullable = false, columnDefinition = "varchar(64) default ''")
    private String password;
}
