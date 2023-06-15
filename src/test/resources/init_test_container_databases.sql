CREATE TABLE Tenancy (
  id SERIAL NOT NULL,
  name VARCHAR(254) NOT NULL,
  userLimit int NOT NULL DEFAULT 1,
  userUsage int NOT NULL DEFAULT 0,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE(name)
);

CREATE TABLE Users (
  id SERIAL NOT NULL,
  tenancyId int NULL,
  password VARCHAR(2000) NOT NULL,
  firstName VARCHAR(45) NOT NULL,
  lastName VARCHAR(45) NOT NULL,
  email VARCHAR(150) NOT NULL,
  enabled BOOLEAN NOT NULL,
  verified BOOLEAN NOT NULL,
  createdAt TIMESTAMP NOT NULL,
  newsletter BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id),
  CONSTRAINT tenancyIdFk FOREIGN KEY (tenancyId) REFERENCES Tenancy (id)
);

CREATE TABLE Authorities (
  id SERIAL NOT NULL,
  category VARCHAR(50) NOT NULL,
  name VARCHAR(45) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE(name)
);

CREATE TABLE UserAuthorities (
  id SERIAL NOT NULL,
  userId int NOT NULL,
  authorityId int NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT authorityIdFk FOREIGN KEY (authorityId) REFERENCES Authorities (id) ON DELETE CASCADE,
  CONSTRAINT userIdFk FOREIGN KEY (userId) REFERENCES Users (id) ON DELETE CASCADE
);

CREATE TABLE Verifications (
  id SERIAL NOT NULL,
  email VARCHAR(150) NOT NULL,
  type VARCHAR(45) NOT NULL,
  status VARCHAR(45) NOT NULL,
  hash VARCHAR(2000) NOT NULL,
  lastChange TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO Authorities (category, name) VALUES ('COMMON_USER','ROLE_USER');
INSERT INTO Authorities (category, name) VALUES ('ADMINISTRATOR','ROLE_ADMIN');
INSERT INTO Authorities (category, name) VALUES ('ORGANISATION','TENANCY_ADMIN');