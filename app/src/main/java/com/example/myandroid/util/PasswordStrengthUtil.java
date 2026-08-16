package com.example.myandroid.util;

public class PasswordStrengthUtil {

    public enum Strength {
        WEAK(0, "弱", 0xFFC75B5B),
        MEDIUM(1, "中", 0xFFC4A35A),
        STRONG(2, "强", 0xFF6B8F5B);

        public final int level;
        public final String label;
        public final int color;

        Strength(int level, String label, int color) {
            this.level = level;
            this.label = label;
            this.color = color;
        }
    }

    public static Strength calculate(String password) {
        if (password == null || password.isEmpty()) {
            return Strength.WEAK;
        }

        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;

        boolean hasLower = false, hasUpper = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        if (hasLower) score++;
        if (hasUpper) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;

        boolean hasSequential = false;
        for (int i = 0; i < password.length() - 2; i++) {
            char c1 = password.charAt(i);
            char c2 = password.charAt(i + 1);
            char c3 = password.charAt(i + 2);
            if (c2 == c1 + 1 && c3 == c2 + 1) {
                hasSequential = true;
                break;
            }
        }
        if (hasSequential) score--;

        boolean hasRepeated = false;
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && password.charAt(i) == password.charAt(i + 2)) {
                hasRepeated = true;
                break;
            }
        }
        if (hasRepeated) score--;

        if (score <= 3) return Strength.WEAK;
        if (score <= 5) return Strength.MEDIUM;
        return Strength.STRONG;
    }

    public static int getProgress(Strength strength) {
        switch (strength) {
            case WEAK: return 33;
            case MEDIUM: return 66;
            case STRONG: return 100;
            default: return 0;
        }
    }
}
