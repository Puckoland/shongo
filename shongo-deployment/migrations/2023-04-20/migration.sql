/**
 * 2023-04-20: add capacity capability
 */
BEGIN TRANSACTION;

/* Create capacity capability */
CREATE TABLE capacity_capability (capacity INT4, id INT8 NOT NULL, PRIMARY KEY (id));
ALTER TABLE capacity_capability ADD CONSTRAINT extends_capability FOREIGN KEY (id) REFERENCES capability;
ALTER TABLE capacity_capability OWNER TO shongo;

COMMIT TRANSACTION;
