INSERT INTO roles(name) VALUES('ROLE_USER') ON CONFLICT DO NOTHING;
INSERT INTO roles(name) VALUES('ROLE_ADMIN') ON CONFLICT DO NOTHING;

INSERT INTO users(username, password, email, first_name, last_name) VALUES('admin', '{bcrypt}$2a$14$cK32EK9nyx2QPdP3/GI7EOj/QIcv4ylyIa0d5N80R1.S1GI20kNPu', 'admin@email', 'Admin', 'Test') ON CONFLICT DO NOTHING;
INSERT INTO users_roles(role_id, user_id) VALUES((SELECT id FROM roles WHERE name='ROLE_ADMIN'), (SELECT id FROM users WHERE username='admin')) ON CONFLICT DO NOTHING;
INSERT INTO users(username, password, email, first_name, last_name) VALUES('user', '{bcrypt}$2a$14$YO4WDleHhXlzo5tO3H0bXOl2wq.sc4ccH3XTauzio7oIevGyT0gr.', 'user@email', 'User', 'Test') ON CONFLICT DO NOTHING;
INSERT INTO users_roles(role_id, user_id) VALUES((SELECT id FROM roles WHERE name='ROLE_USER'), (SELECT id FROM users WHERE username='user')) ON CONFLICT DO NOTHING;