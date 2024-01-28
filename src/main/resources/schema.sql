CREATE TABLE IF NOT EXISTS task
(

    id          INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(100) NOT NULL,
    status      BOOLEAN      NOT NULL

)