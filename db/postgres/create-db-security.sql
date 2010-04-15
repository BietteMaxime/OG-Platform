/* Security Master */

/* requires create-db-common.sql */

create table sec_currency (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_commodityfuturetype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_bondfuturetype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_cashrate (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_unit (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_domain_specific_identifier_association (
    id int8 not null,
    security_discriminator varchar(255),
    security_id int8,
    domain varchar(255) not null,
    identifier varchar(255) not null,
    validStartDate date,
    validEndDate date,
    primary key (id),
    unique (domain, identifier, validStartDate, validEndDate)
);

create table sec_exchange (
    id int8 not null,
    name varchar(255) not null unique,
    description varchar(255),
    primary key (id)
);

create table sec_gics (
  id int8 not null,
  name varchar(8) not null unique,
  description varchar(255),
  primary key (id)
);

create table sec_equity (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    exchange_id int8 not null,
    companyName varchar(255) not null,
    currency_id int8 not null,
    gicscode_id int8,
    primary key (id),
  	constraint fk_equity2currency foreign key (currency_id) references sec_currency(id),
  	constraint fk_equity2exchange foreign key (exchange_id) references sec_exchange(id),
  	constraint fk_equity2gics foreign key (gicscode_id) references sec_gics(id)
);

create table sec_option (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    option_security_type varchar(32) not null,
    option_type varchar(32) not null,
    strike double precision not null,
    expiry date not null,
    underlyingIdentityKey varchar(255),
    power double precision,
    currency1_id int8 not null,
    currency2_id int8,
    currency3_id int8,
    exchange_id int8,
    counterparty varchar(255),
    margined bool,
    primary key (id),
    constraint fk_option2currency1 foreign key (currency1_id) references sec_currency (id),
    constraint fk_option2currency2 foreign key (currency2_id) references sec_currency (id),
    constraint fk_option2currency3 foreign key (currency3_id) references sec_currency (id),
    constraint fk_option2exchange foreign key (exchange_id) references sec_exchange (id)
);

create table sec_frequency (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_daycount (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_businessdayconvention (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
);

create table sec_issuertype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_market (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_yieldconvention (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_guaranteetype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_coupontype (
    id int8 not null,
    name varchar(255) not null unique,
    primary key (id)
 );

create table sec_bond (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    bond_type varchar(32) not null,
    issuername varchar(255) not null,
    issuertype_id int8 not null,
    issuerdomicile varchar(255) not null,
    market_id int8 not null,
    currency_id int8 not null,
    yieldconvention_id int8 not null,
    guaranteetype_id int8 not null,
    maturity date not null,
    coupontype_id int8 not null,
    couponrate double precision not null,
    couponfrequency_id int8 not null,
    daycountconvention_id int8 not null,
    businessdayconvention_id int8 not null,
    announcementdate date not null,
    interestaccrualdate date not null,
    settlementdate date not null,
    firstcoupondate date not null,
    issuanceprice double precision not null,
    totalamountissued double precision not null,
    minimumamount double precision not null,
    minimumincrement double precision not null,
    paramount double precision not null,
    redemptionvalue double precision not null,
    primary key (id),
    constraint fk_bond2issuertype foreign key (issuertype_id) references sec_issuertype (id),
    constraint fk_bond2market foreign key (market_id) references sec_market (id),
    constraint fk_bond2currency foreign key (currency_id) references sec_currency (id),
    constraint fk_bond2yieldconvention foreign key (yieldconvention_id) references sec_yieldconvention (id),
    constraint fk_bond2guaranteetype foreign key (guaranteetype_id) references sec_guaranteetype (id),
    constraint fk_bond2coupontype foreign key (coupontype_id) references sec_coupontype (id),
    constraint fk_bond2frequency foreign key (couponfrequency_id) references sec_frequency (id),
    constraint fk_bond2daycount foreign key (daycountconvention_id) references sec_daycount (id),
    constraint fk_bond2businessdayconvention foreign key (businessdayconvention_id) references sec_businessdayconvention (id)
);

create table sec_future (
    id int8 not null,
    effectiveDateTime date not null,
    deleted bool not null,
    lastModifiedDateTime date not null,
    lastModifiedBy varchar(255),
    displayName varchar(255) not null,
    first_version_descriminator varchar(255),
    first_version_id int8,
    future_type varchar(32) not null,
    expiry date not null,
    tradingexchange_id int8 not null,
    settlementexchange_id int8 not null,
    currency1_id int8,
    currency2_id int8,
    currency3_id int8,
    bondtype_id int8,
    commoditytype_id int8,
    cashratetype_id int8,
    unitname_id int8,
    unitnumber double precision, 
    primary key (id),
    constraint fk_future2exchange1 foreign key (tradingexchange_id) references sec_exchange (id),
    constraint fk_future2exchange2 foreign key (settlementexchange_id) references sec_exchange (id),
    constraint fk_future2currency1 foreign key (currency1_id) references sec_currency (id),
    constraint fk_future2currency2 foreign key (currency2_id) references sec_currency (id),
    constraint fk_future2currency3 foreign key (currency3_id) references sec_currency (id),
    constraint fk_future2bondfuturetype foreign key (bondtype_id) references sec_bondfuturetype (id),
    constraint fk_future2commodityfuturetype foreign key (commoditytype_id) references sec_commodityfuturetype (id),
    constraint fk_future2cashrate foreign key (cashratetype_id) references sec_cashrate (id),
    constraint fk_future2unit foreign key (unitname_id) references sec_unit (id)
);

create table sec_future_basket (
    id int8 not null,
    future_id int8 not null,
    domain varchar(255) not null,
    identifier varchar(255) not null,
    primary key (id),
    constraint fk_future_basket2future foreign key (future_id) references sec_future (id),
    unique (future_id, domain, identifier)
);
