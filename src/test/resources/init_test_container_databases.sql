CREATE TABLE `Users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `userName` varchar(45) NOT NULL,
  `password` varchar(2000) NOT NULL,
  `firstName` varchar(45) NOT NULL,
  `lastName` varchar(45) NOT NULL,
  `email` varchar(150) NOT NULL,
  `enabled` tinyint NOT NULL,
  `verified` tinyint NOT NULL,
  `createdAt` datetime NOT NULL,
  `authorities` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `Authorities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category` int NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`,`category`,`name`),
  UNIQUE KEY `name_UNIQUE` (`name`)
);

CREATE TABLE `UserAuthorities` (
  `id` int NOT NULL AUTO_INCREMENT,
  `userId` int NOT NULL,
  `authorityId` int DEFAULT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `userIdFk_idx` (`userId`),
  KEY `authorityIdFk_idx` (`authorityId`),
  CONSTRAINT `authorityIdFk` FOREIGN KEY (`authorityId`) REFERENCES `Authorities` (`id`) ON DELETE CASCADE,
  CONSTRAINT `userIdFk` FOREIGN KEY (`userId`) REFERENCES `Users` (`id`) ON DELETE CASCADE
)

