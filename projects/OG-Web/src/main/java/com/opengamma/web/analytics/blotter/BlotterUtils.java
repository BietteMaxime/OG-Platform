/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.conversion.JodaBeanConverters;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.LongShort;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingGearingIRLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 *
 */
/* package */ class BlotterUtils {

  // TODO this should be configurable, should be able to add from client projects
  /** All the securities and related types supported by the blotter. */
  private static final Set<MetaBean> s_metaBeans = Sets.<MetaBean>newHashSet(
      FXForwardSecurity.meta(),
      SwapSecurity.meta(),
      SwaptionSecurity.meta(),
      CapFloorCMSSpreadSecurity.meta(),
      NonDeliverableFXOptionSecurity.meta(),
      FXOptionSecurity.meta(),
      FRASecurity.meta(),
      CapFloorSecurity.meta(),
      EquityVarianceSwapSecurity.meta(),
      FXBarrierOptionSecurity.meta(),
      FixedInterestRateLeg.meta(),
      FloatingInterestRateLeg.meta(),
      FloatingSpreadIRLeg.meta(),
      FloatingGearingIRLeg.meta(),
      InterestRateNotional.meta());

  /** Meta bean factory for looking up meta beans by type name. */
  private static final MetaBeanFactory s_metaBeanFactory = new MapMetaBeanFactory(s_metaBeans);

  /**
   * For traversing trade and security {@link MetaBean}s and building instances from the data sent from the blotter.
   * The security type name is filtered out because it is a read-only property. The external ID bundle is filtered
   * out because it is always empty for trades and securities entered via the blotter but isn't nullable. Therefore
   * it has to be explicitly set to an empty bundle after the client data is processed but before the object is built.
   */
  private static final BeanTraverser s_beanBuildingTraverser = new BeanTraverser(
      new PropertyFilter(FinancialSecurity.meta().externalIdBundle()),
      new PropertyFilter(ManageableSecurity.meta().securityType()));

  /** For converting between strings values used by the UI and real objects. */
  private static final StringConvert s_stringConvert;
  /** For converting property values when creating trades and securities from JSON. */
  private static final Converters s_beanBuildingConverters;
  /** For converting property values when creating JSON objects from trades and securities. */
  private static final Converters s_jsonBuildingConverters;

  static {
    StringToRegionIdConverter stringToRegionIdConverter = new StringToRegionIdConverter();
    // for building beans from JSON
    Map<MetaProperty<?>, Converter<?, ?>> beanRegionConverters = Maps.newHashMap();
    beanRegionConverters.putAll(
        ImmutableMap.<MetaProperty<?>, Converter<?, ?>>of(
            CashSecurity.meta().regionId(), stringToRegionIdConverter,
            CreditDefaultSwapSecurity.meta().regionId(), stringToRegionIdConverter,
            EquityVarianceSwapSecurity.meta().regionId(), stringToRegionIdConverter,
            FRASecurity.meta().regionId(), stringToRegionIdConverter,
            SwapLeg.meta().regionId(), stringToRegionIdConverter));
    beanRegionConverters.putAll(
        ImmutableMap.<MetaProperty<?>, Converter<?, ?>>of(
            FXForwardSecurity.meta().regionId(), new FXRegionConverter(),
            NonDeliverableFXForwardSecurity.meta().regionId(), new FXRegionConverter()));

    // for building JSON from beans
    RegionIdToStringConverter regionIdToStringConverter = new RegionIdToStringConverter();
    Map<MetaProperty<?>, Converter<?, ?>> jsonRegionConverters =
        ImmutableMap.<MetaProperty<?>, Converter<?, ?>>of(
            CashSecurity.meta().regionId(), regionIdToStringConverter,
            CreditDefaultSwapSecurity.meta().regionId(), regionIdToStringConverter,
            EquityVarianceSwapSecurity.meta().regionId(), regionIdToStringConverter,
            FRASecurity.meta().regionId(), regionIdToStringConverter,
            SwapLeg.meta().regionId(), regionIdToStringConverter);

    s_stringConvert = new StringConvert();
    s_stringConvert.register(Frequency.class, new JodaBeanConverters.FrequencyConverter());
    s_stringConvert.register(Currency.class, new JodaBeanConverters.CurrencyConverter());
    s_stringConvert.register(DayCount.class, new JodaBeanConverters.DayCountConverter());
    s_stringConvert.register(ExternalId.class, new JodaBeanConverters.ExternalIdConverter());
    s_stringConvert.register(ExternalIdBundle.class, new JodaBeanConverters.ExternalIdBundleConverter());
    s_stringConvert.register(CurrencyPair.class, new JodaBeanConverters.CurrencyPairConverter());
    s_stringConvert.register(ObjectId.class, new JodaBeanConverters.ObjectIdConverter());
    s_stringConvert.register(UniqueId.class, new JodaBeanConverters.UniqueIdConverter());
    s_stringConvert.register(Expiry.class, new ExpiryConverter());
    s_stringConvert.register(ExerciseType.class, new JodaBeanConverters.ExerciseTypeConverter());
    s_stringConvert.register(BusinessDayConvention.class, new JodaBeanConverters.BusinessDayConventionConverter());
    s_stringConvert.register(YieldConvention.class, new JodaBeanConverters.YieldConventionConverter());
    s_stringConvert.register(MonitoringType.class, new EnumConverter<MonitoringType>());
    s_stringConvert.register(BarrierType.class, new EnumConverter<BarrierType>());
    s_stringConvert.register(BarrierDirection.class, new EnumConverter<BarrierDirection>());
    s_stringConvert.register(SamplingFrequency.class, new EnumConverter<SamplingFrequency>());
    s_stringConvert.register(LongShort.class, new EnumConverter<LongShort>());
    s_stringConvert.register(OptionType.class, new EnumConverter<OptionType>());
    s_stringConvert.register(GICSCode.class, new GICSCodeConverter());
    s_stringConvert.register(ZonedDateTime.class, new ZonedDateTimeConverter());
    s_stringConvert.register(OffsetTime.class, new OffsetTimeConverter());
    s_stringConvert.register(Country.class, new CountryConverter());

    s_jsonBuildingConverters = new Converters(jsonRegionConverters, s_stringConvert);
    s_beanBuildingConverters = new Converters(beanRegionConverters, s_stringConvert);
  }

  /**
   * Filters out region ID for FX forwards when building JSON for the security and HTML screens showing the structure.
   * The property value is hard-coded to {@code FINANCIAL_REGION~GB} for FX forwards so its value is of no interest
   * to the client and it can't be updated.
   */
  private static final PropertyFilter s_fxRegionFilter =
      new PropertyFilter(FXForwardSecurity.meta().regionId(), NonDeliverableFXForwardSecurity.meta().regionId());

  /**
   * Filters out the {@code externalIdBundle} property from OTC securities when building the HTML showing the security
   * structure. OTC security details are passed to the blotter back end which generates the ID so this
   * info is irrelevant to the client.
   */
  private static final BeanVisitorDecorator s_externalIdBundleFilter = new PropertyNameFilter("externalIdBundle");

  /**
   * Filters out the underlying ID field of {@link SwaptionSecurity} when building the HTML showing the security
   * structure. The back end creates the underlying security and fills this field in so it's of no interest
   * to the client.
   */
  private static final PropertyFilter s_swaptionUnderlyingFilter = new PropertyFilter(SwaptionSecurity.meta().underlyingId());

  /**
   * Filters out the {@code securityType} field for all securities when building the HTML showing the security
   * structure. This value is read-only in each security type and is of no interest to the client.
   */
  private static final PropertyFilter s_securityTypeFilter = new PropertyFilter(ManageableSecurity.meta().securityType());

  @SuppressWarnings("unchecked")
  /* package */ static FinancialSecurity buildSecurity(BeanDataSource data) {
    BeanVisitor<BeanBuilder<FinancialSecurity>> visitor = new BeanBuildingVisitor<>(data, s_metaBeanFactory,
                                                                                    s_beanBuildingConverters);
    MetaBean metaBean = s_metaBeanFactory.beanFor(data);
    // TODO check it's a FinancialSecurity metaBean
    if (!(metaBean instanceof FinancialSecurity.Meta)) {
      throw new IllegalArgumentException("MetaBean " + metaBean + " isn't for a FinancialSecurity");
    }
    BeanBuilder<FinancialSecurity> builder = (BeanBuilder<FinancialSecurity>) s_beanBuildingTraverser.traverse(metaBean, visitor);
    // externalIdBundle needs to be specified or building fails because it's not nullable
    // TODO need to preserve the bundle when editing existing trades. pass to client or use previous version?
    // might need to return BeanBuilder from this method and do this outside
    builder.set(FinancialSecurity.meta().externalIdBundle(), ExternalIdBundle.EMPTY);
    Object bean = builder.build();
    if (bean instanceof FinancialSecurity) {
      return (FinancialSecurity) bean;
    } else {
      throw new IllegalArgumentException("object type " + bean.getClass().getName() + " isn't a Financial Security");
    }
  }

  // TODO move to BlotterUtils
  /* package */ static StringConvert getStringConvert() {
    return s_stringConvert;
  }

  /* package */ static Converters getJsonBuildingConverters() {
    return s_jsonBuildingConverters;
  }

  /* package */ static Converters getBeanBuildingConverters() {
    return s_beanBuildingConverters;
  }

  /* package */ static Set<MetaBean> getMetaBeans() {
    return s_metaBeans;
  }

  /* package */ static BeanTraverser structureBuildingTraverser() {
    return new BeanTraverser(s_externalIdBundleFilter, s_securityTypeFilter, s_swaptionUnderlyingFilter, s_fxRegionFilter);
  }

  /* package */
  static BeanTraverser securityJsonBuildingTraverser() {
    return new BeanTraverser(s_securityTypeFilter, s_fxRegionFilter);
  }
}

// ----------------------------------------------------------------------------------


/**
 * For converting between enum instances and strings. The enum value names are made more readable by downcasing
 * and capitalizing them and replacing underscores with spaces.
 * @param <T> Type of the enum
 */
/* package */ class EnumConverter<T extends Enum> implements StringConverter<T> {

  @Override
  public T convertFromString(Class<? extends T> type, String str) {
    // IntelliJ says this cast is redundant but javac disagrees
    //noinspection RedundantCast
    return (T) Enum.valueOf(type, str.toUpperCase().replace(' ', '_'));
  }

  @Override
  public String convertToString(T e) {
    return WordUtils.capitalize(e.name().toLowerCase().replace('_', ' '));
  }
}

/**
 * For converting between strings and {@link GICSCode}.
 */
/* package */ class GICSCodeConverter implements StringConverter<GICSCode> {

  @Override
  public GICSCode convertFromString(Class<? extends GICSCode> cls, String code) {
    return GICSCode.of(code);
  }

  @Override
  public String convertToString(GICSCode code) {
    return code.getCode();
  }
}

/**
 * Converts {@link ZonedDateTime} to a local date string (e.g. 2012-12-21) and creates a {@link ZonedDateTime} from
 * a local date string with a time of 11:00 and a zone of UTC.
 */
/* package */ class ZonedDateTimeConverter implements StringConverter<ZonedDateTime> {

  @Override
  public ZonedDateTime convertFromString(Class<? extends ZonedDateTime> cls, String localDateString) {
    LocalDate localDate = LocalDate.parse(localDateString);
    return localDate.atTime(11, 0).atZone(ZoneOffset.UTC);
  }

  @Override
  public String convertToString(ZonedDateTime dateTime) {
    return dateTime.getDate().toString();
  }
}

/**
 * Converts an {@link OffsetTime} to a time string (e.g. 11:35) and discards the offset. Creates
 * an {@link OffsetTime} instance by parsing a local date string and using UTC as the offset.
 */
/* package */ class OffsetTimeConverter implements StringConverter<OffsetTime> {

  @Override
  public OffsetTime convertFromString(Class<? extends OffsetTime> cls, String timeString) {
    return OffsetTime.of(LocalTime.parse(timeString), ZoneOffset.UTC);
  }

  @Override
  public String convertToString(OffsetTime time) {
    return time.getTime().toString();
  }
}

/**
 * Converts between an {@link Expiry} and a local date string (e.g. 2011-03-08).
 */
/* package */ class ExpiryConverter implements StringConverter<Expiry> {

  @Override
  public Expiry convertFromString(Class<? extends Expiry> cls, String localDateString) {
    LocalDate localDate = LocalDate.parse(localDateString);
    return new Expiry(localDate.atTime(11, 0).atZone(ZoneOffset.UTC));
  }

  @Override
  public String convertToString(Expiry expiry) {
    return expiry.getExpiry().getDate().toString();
  }
}

/**
 * Converts between an {@link Expiry} and a local date string (e.g. 2011-03-08).
 */
/* package */ class CountryConverter implements StringConverter<Country> {

  @Override
  public Country convertFromString(Class<? extends Country> cls, String countryCode) {
    return Country.of(countryCode);
  }

  @Override
  public String convertToString(Country country) {
    return country.getCode();
  }
}

/**
 * Converts a string to an {@link ExternalId} with a scheme of {@link ExternalSchemes#FINANCIAL}.
 */
/* package */ class StringToRegionIdConverter implements Converter<String, ExternalId> {

  /**
   * Converts a string to an {@link ExternalId} with a scheme of {@link ExternalSchemes#FINANCIAL}.
   * @param region The region name, not empty
   * @return An {@link ExternalId} with a scheme of {@link ExternalSchemes#FINANCIAL} and a value of {@code region}.
   */
  @Override
  public ExternalId convert(String region) {
    if (StringUtils.isEmpty(region)) {
      throw new IllegalArgumentException("Region must not be empty");
    }
    return ExternalId.of(ExternalSchemes.FINANCIAL, region);
  }
}

/**
 * Converts an {@link ExternalId} to a string.
 */
/* package */ class RegionIdToStringConverter implements Converter<ExternalId, String> {

  /**
   * Converts an {@link ExternalId} to a string
   * @param regionId The region ID, not null
   * @return {@code regionId}'s value
   */
  @Override
  public String convert(ExternalId regionId) {
    ArgumentChecker.notNull(regionId, "regionId");
    return regionId.getValue();
  }
}