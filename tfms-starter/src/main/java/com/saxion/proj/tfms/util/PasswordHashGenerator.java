package com.saxion.proj.tfms.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 临时工具类：用于生成BCrypt密码哈希
 * 使用后可以删除此文件
 * 
 * 运行方法：
 * 1. 在main方法中设置你要哈希的密码
 * 2. 运行此类
 * 3. 复制输出的哈希值用于SQL插入语句
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成常用测试密码的哈希值
        System.out.println("=== 测试用户密码哈希 ===");
        System.out.println();
        
        System.out.println("Admin123 的哈希值：");
        System.out.println(encoder.encode("Admin123"));
        System.out.println();
        
        System.out.println("Driver123 的哈希值：");
        System.out.println(encoder.encode("Driver123"));
        System.out.println();
        
        System.out.println("Planner123 的哈希值：");
        System.out.println(encoder.encode("Planner123"));
        System.out.println();
        
        // 如果你需要其他密码，取消注释下面的代码
        // System.out.println("你的密码 的哈希值：");
        // System.out.println(encoder.encode("你的密码"));
    }
}
