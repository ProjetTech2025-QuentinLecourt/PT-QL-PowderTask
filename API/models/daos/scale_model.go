package daos

type Scale struct {
	ID        uint   `json:"id" gorm:"primaryKey;autoIncrement;not null"`
	ScaleName string `json:"scale_name" gorm:"type:varchar(50);not null"`
	Users     []User `gorm:"many2many:users_scales;"`
}
