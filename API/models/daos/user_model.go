package daos

type User struct {
	ID        uint   `json:"id" gorm:"primaryKey;autoIncrement;not null"`
	FirstName string `json:"first_name" gorm:"type:varchar(50);not null"`
	LastName  string `json:"last_name" gorm:"type:varchar(50);not null"`
	Email     string `json:"email" gorm:"type:varchar(100);unique;not null"`
	Password  string `json:"password" gorm:"type:varchar(100);not null"`
}
