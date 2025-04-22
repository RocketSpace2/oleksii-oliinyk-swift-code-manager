CREATE TABLE swift_code (
  swift_code_id SERIAL PRIMARY KEY,
  swift_code        VARCHAR(11)  NOT NULL UNIQUE,
  bank_name         VARCHAR(255) NOT NULL,
  address           TEXT         NOT NULL,
  country_iso2_code CHAR(2)      NOT NULL,
  country_name      VARCHAR(100) NOT NULL,
  headquarter_swift_code_id INTEGER,
  CONSTRAINT fk_headquarter
    FOREIGN KEY (headquarter_swift_code_id)
    REFERENCES swift_code (swift_code_id)
    ON DELETE SET NULL
    ON UPDATE CASCADE
);