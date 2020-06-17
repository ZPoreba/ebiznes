# --- !Ups
CREATE TABLE "logininfo" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "providerID" VARCHAR NOT NULL,
    "providerKey" VARCHAR NOT NULL
);

CREATE TABLE "userlogininfo" (
    "userID" VARCHAR NOT NULL,
    "loginInfoId" INT NOT NULL
);

CREATE TABLE "passwordinfo" (
    "hasher" VARCHAR NOT NULL,
    "password" VARCHAR NOT NULL,
    "salt" VARCHAR,
    "loginInfoId" INT NOT NULL
);

CREATE TABLE "oauth1info" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "token" VARCHAR NOT NULL,
    "secret" VARCHAR NOT NULL,
    "loginInfoId" INT NOT NULL
);

CREATE TABLE "oauth2info" (
    "id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    "accesstoken" VARCHAR NOT NULL,
    "tokentype" VARCHAR,
    "expiresin" INTEGER,
    "refreshtoken" VARCHAR,
    "logininfoid" INT NOT NULL
);

CREATE TABLE "openidinfo" (
    "id" VARCHAR NOT NULL PRIMARY KEY,
    "logininfoid" BIGINT NOT NULL
);

CREATE TABLE "openidattributes" (
    "id" VARCHAR NOT NULL,
    "key" VARCHAR NOT NULL,
    "value" VARCHAR NOT NULL
);

# --- !Downs
DROP TABLE "openidattributes"
DROP TABLE "openidinfo"
DROP TABLE "oauth2info"
DROP TABLE "oauth1info"
DROP TABLE "passwordinfo"
DROP TABLE "userlogininfo"
DROP TABLE "logininfo"