package models

import "github.com/dgrijalva/jwt-go"

// Claims représente la structure du JWT
type Claims struct {
	FirstName string `json:"firstname"`
	Email     string `json:"email"`
	Job       string `json:"job"`
	jwt.StandardClaims
}
