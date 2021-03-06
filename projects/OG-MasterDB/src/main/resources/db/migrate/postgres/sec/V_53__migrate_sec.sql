START TRANSACTION;

  -- update the version
  UPDATE sec_schema_version SET version_value='53' WHERE version_key='schema_patch';

  CREATE TABLE sec_bondfutureoption (
      id bigint NOT NULL,
      security_id bigint NOT NULL,
      option_exercise_type varchar(32) NOT NULL,
      option_type varchar(32) NOT NULL,
      strike double precision NOT NULL,
      expiry_date timestamp without time zone NOT NULL,
      expiry_zone varchar(50) NOT NULL,
      expiry_accuracy smallint NOT NULL,
      underlying_scheme varchar(255) NOT NULL,
      underlying_identifier varchar(255) NOT NULL,
      currency_id bigint NOT NULL,
      trading_exchange_id bigint NOT NULL,
      settlement_exchange_id bigint NOT NULL,
      pointValue double precision NOT NULL,
      PRIMARY KEY (id),
      CONSTRAINT sec_fk_bondfutureoption2sec FOREIGN KEY (security_id) REFERENCES sec_security (id),
      CONSTRAINT sec_fk_bondfutureoption2currency FOREIGN KEY (currency_id) REFERENCES sec_currency (id),
      CONSTRAINT sec_fk_bondfutureoption2trading_exchange FOREIGN KEY (trading_exchange_id) REFERENCES sec_exchange (id),
      CONSTRAINT sec_fk_bondfutureoption2settlement_exchange FOREIGN KEY (settlement_exchange_id) REFERENCES sec_exchange (id)
  );
  
COMMIT;
