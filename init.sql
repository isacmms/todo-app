CREATE TABLE IF NOT EXISTS users (
	id BIGSERIAL NOT NULL,
	version BIGINT,
	
	username VARCHAR(50) UNIQUE NOT NULL,
	password VARCHAR(255) NOT NULL,
	email VARCHAR(50) UNIQUE NOT NULL,
	first_name VARCHAR(50),
	last_name VARCHAR(50),
	
	account_non_expired boolean,
	account_non_locked boolean,
	credentials_non_expired boolean,
	enabled boolean,
	
	created_date TIMESTAMP,
	created_by VARCHAR(50),
	last_modified_date TIMESTAMP,
	last_modified_by VARCHAR(50),
	
	PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS roles (
	id BIGSERIAL NOT NULL,
	version BIGINT,
	
	name VARCHAR(25) UNIQUE,
	
	created_date TIMESTAMP,
	created_by VARCHAR(50),
	last_modified_date TIMESTAMP,
	last_modified_by VARCHAR(50),
	
	PRIMARY KEY(id)
);

CREATE TABLE IF NOT EXISTS users_roles (
	id BIGSERIAL NOT NULL,
	version BIGINT,
	
	user_id BIGINT NOT NULL,
	role_id BIGINT NOT NULL,
	
	created_date TIMESTAMP,
	created_by VARCHAR(50),
	last_modified_date TIMESTAMP,
	last_modified_by VARCHAR(50),
	
	PRIMARY KEY(user_id, role_id)
);