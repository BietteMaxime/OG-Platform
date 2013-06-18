/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Period;

import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.DiscountFactorNode;
import com.opengamma.financial.analytics.ircurve.strips.FRANode;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * Contains fudge builders for curve nodes.
 */
/* package */ final class CurveNodeBuilders {
  /** The curve node id mapper field */
  private static final String CURVE_MAPPER_ID_FIELD = "curveNodeIdMapper";
  /** The data field field */
  private static final String DATA_FIELD_FIELD = "dataField";

  private CurveNodeBuilders() {
  }

  /**
   * Fudge builder for {@link CurveNodeWithIdentifier}
   */
  @FudgeBuilderFor(CurveNodeWithIdentifier.class)
  public static class CurveNodeWithIdentifierBuilder implements FudgeBuilder<CurveNodeWithIdentifier> {
    /** The curve strip field */
    private static final String CURVE_STRIP_FIELD = "curveStrip";
    /** The id field */
    private static final String ID_FIELD = "id";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CurveNodeWithIdentifier object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      serializer.addToMessageWithClassHeaders(message, CURVE_STRIP_FIELD, null, object.getCurveNode());
      serializer.addToMessage(message, ID_FIELD, null, object.getIdentifier());
      return message;
    }

    @Override
    public CurveNodeWithIdentifier buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final CurveNode curveStrip = (CurveNode) deserializer.fieldValueToObject(message.getByName(CURVE_STRIP_FIELD));
      final ExternalId id = deserializer.fieldValueToObject(ExternalId.class, message.getByName(ID_FIELD));
      return new CurveNodeWithIdentifier(curveStrip, id);
    }

  }

  /**
   * Fudge builder for {@link CashNode}
   */
  @FudgeBuilderFor(CashNode.class)
  public static class CashNodeBuilder implements FudgeBuilder<CashNode> {
    /** The start tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The maturity tenor field */
    private static final String MATURITY_TENOR_FIELD = "maturityTenor";
    /** The convention field */
    private static final String CONVENTION_ID_FIELD = "convention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CashNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(START_TENOR_FIELD, object.getStartTenor().getPeriod().toString());
      message.add(MATURITY_TENOR_FIELD, object.getMaturityTenor().getPeriod().toString());
      message.add(CONVENTION_ID_FIELD, object.getConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(DATA_FIELD_FIELD, object.getDataField());
      return message;
    }

    @Override
    public CashNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Tenor startTenor = new Tenor(Period.parse(message.getString(START_TENOR_FIELD)));
      final Tenor maturityTenor = new Tenor(Period.parse(message.getString(MATURITY_TENOR_FIELD)));
      final ExternalId conventionId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(CONVENTION_ID_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      final String dataField = message.getString(DATA_FIELD_FIELD);
      return new CashNode(startTenor, maturityTenor, conventionId, curveNodeIdMapperName, dataField);
    }
  }

  @FudgeBuilderFor(ContinuouslyCompoundedRateNode.class)
  public static class ContinuouslyCompoundedRateNodeBuilder implements FudgeBuilder<ContinuouslyCompoundedRateNode> {
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ContinuouslyCompoundedRateNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(TENOR_FIELD, object.getTenor());
      message.add(DATA_FIELD_FIELD, object.getDataField());
      return message;
    }

    @Override
    public ContinuouslyCompoundedRateNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      //TODO should just use Period string for these objects
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(TENOR_FIELD));
      final String dataField = message.getString(DATA_FIELD_FIELD);
      final ContinuouslyCompoundedRateNode strip = new ContinuouslyCompoundedRateNode(curveNodeIdMapperName, tenor, dataField);
      return strip;
    }
  }

  @FudgeBuilderFor(CreditSpreadNode.class)
  public static class CreditSpreadNodeBuilder implements FudgeBuilder<CreditSpreadNode> {
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CreditSpreadNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(TENOR_FIELD, object.getTenor());
      message.add(DATA_FIELD_FIELD, object.getDataField());
      return message;
    }

    @Override
    public CreditSpreadNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(TENOR_FIELD));
      final String dataField = message.getString(DATA_FIELD_FIELD);
      final CreditSpreadNode strip = new CreditSpreadNode(curveNodeIdMapperName, tenor, dataField);
      return strip;
    }
  }

  @FudgeBuilderFor(DiscountFactorNode.class)
  public static class DiscountFactorNodeBuilder implements FudgeBuilder<DiscountFactorNode> {
    private static final String TENOR_FIELD = "tenor";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final DiscountFactorNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(TENOR_FIELD, object.getTenor());
      message.add(DATA_FIELD_FIELD, object.getDataField());
      return message;
    }

    @Override
    public DiscountFactorNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      //TODO should just use the period string for Tenor
      final Tenor tenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(TENOR_FIELD));
      final String dataField = message.getString(DATA_FIELD_FIELD);
      if (dataField == null) {
        return new DiscountFactorNode(curveNodeIdMapperName, tenor);
      }
      return new DiscountFactorNode(curveNodeIdMapperName, tenor, dataField);
    }
  }

  /**
   * Fudge builder for {@link FRANode}
   */
  @FudgeBuilderFor(FRANode.class)
  public static class FRANodeBuilder implements FudgeBuilder<FRANode> {
    /** The fixing start field */
    private static final String FIXING_START_FIELD = "fixingStart";
    /** The fixing end field */
    private static final String FIXING_END_FIELD = "fixingEnd";
    /** The convention field */
    private static final String CONVENTION_ID_FIELD = "convention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FRANode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(FIXING_START_FIELD, object.getFixingStart().getPeriod().toString());
      message.add(FIXING_END_FIELD, object.getFixingEnd().getPeriod().toString());
      message.add(CONVENTION_ID_FIELD, object.getConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(DATA_FIELD_FIELD, object.getDataField());
      return message;
    }

    @Override
    public FRANode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      final Tenor fixingStart = new Tenor(Period.parse(message.getString(FIXING_START_FIELD)));
      final Tenor fixingEnd = new Tenor(Period.parse(message.getString(FIXING_END_FIELD)));
      final ExternalId conventionId = deserializer.fieldValueToObject(ExternalId.class, message.getByName(CONVENTION_ID_FIELD));
      final String dataField = message.getString(DATA_FIELD_FIELD);
      return new FRANode(fixingStart, fixingEnd, conventionId, curveNodeIdMapperName, dataField);
    }

  }

  @FudgeBuilderFor(RateFutureNode.class)
  public static class RateFutureNodeBuilder implements FudgeBuilder<RateFutureNode> {
    private static final String FUTURE_NUMBER_FIELD = "futureNumber";
    private static final String START_TENOR_FIELD = "startTenor";
    private static final String FUTURE_TENOR_FIELD = "futureTenor";
    private static final String UNDERLYING_TENOR_FIELD = "underlyingTenor";
    private static final String FUTURE_CONVENTION_FIELD = "futureConvention";
    private static final String UNDERLYING_CONVENTION_FIELD = "underlyingConvention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final RateFutureNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(FUTURE_NUMBER_FIELD, object.getFutureNumber());
      message.add(START_TENOR_FIELD, object.getStartTenor());
      message.add(FUTURE_TENOR_FIELD, object.getFutureTenor());
      message.add(UNDERLYING_TENOR_FIELD, object.getUnderlyingTenor());
      message.add(FUTURE_CONVENTION_FIELD, object.getFutureConvention());
      message.add(UNDERLYING_CONVENTION_FIELD, object.getUnderlyingConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(DATA_FIELD_FIELD, object.getDataField());
      return message;
    }

    @Override
    public RateFutureNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final int futureNumber = message.getInt(FUTURE_NUMBER_FIELD);
      final Tenor startTenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(START_TENOR_FIELD));
      final Tenor futureTenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(FUTURE_TENOR_FIELD));
      final Tenor underlyingTenor = deserializer.fieldValueToObject(Tenor.class, message.getByName(UNDERLYING_TENOR_FIELD));
      final ExternalId futureConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(FUTURE_CONVENTION_FIELD));
      final ExternalId underlyingConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(UNDERLYING_CONVENTION_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      final String dataField = message.getString(DATA_FIELD_FIELD);
      return new RateFutureNode(futureNumber, startTenor, futureTenor, underlyingTenor, futureConvention, underlyingConvention, curveNodeIdMapperName,
          dataField);
    }

  }

  /**
   * Fudge builder for {@link SwapNode}
   */
  @FudgeBuilderFor(SwapNode.class)
  public static final class SwapNodeBuilder implements FudgeBuilder<SwapNode> {
    /** The start tenor field */
    private static final String START_TENOR_FIELD = "startTenor";
    /** The maturity tenor field */
    private static final String MATURITY_TENOR_FIELD = "maturityTenor";
    /** The pay leg convention field */
    private static final String PAY_LEG_CONVENTION_FIELD = "payLegConvention";
    /** The receive leg convention field */
    private static final String RECEIVE_LEG_CONVENTION_FIELD = "receiveLegConvention";

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final SwapNode object) {
      final MutableFudgeMsg message = serializer.newMessage();
      message.add(null, 0, object.getClass().getName());
      message.add(START_TENOR_FIELD, object.getStartTenor().getPeriod().toString());
      message.add(MATURITY_TENOR_FIELD, object.getMaturityTenor().getPeriod().toString());
      message.add(PAY_LEG_CONVENTION_FIELD, object.getPayLegConvention());
      message.add(RECEIVE_LEG_CONVENTION_FIELD, object.getReceiveLegConvention());
      message.add(CURVE_MAPPER_ID_FIELD, object.getCurveNodeIdMapperName());
      message.add(DATA_FIELD_FIELD, object.getDataField());
      return message;
    }

    @Override
    public SwapNode buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Tenor startTenor = new Tenor(Period.parse(message.getString(START_TENOR_FIELD)));
      final Tenor maturityTenor = new Tenor(Period.parse(message.getString(MATURITY_TENOR_FIELD)));
      final ExternalId payLegConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(PAY_LEG_CONVENTION_FIELD));
      final ExternalId receiveLegConvention = deserializer.fieldValueToObject(ExternalId.class, message.getByName(RECEIVE_LEG_CONVENTION_FIELD));
      final String curveNodeIdMapperName = message.getString(CURVE_MAPPER_ID_FIELD);
      final String dataField = message.getString(DATA_FIELD_FIELD);
      return new SwapNode(startTenor, maturityTenor, payLegConvention, receiveLegConvention, curveNodeIdMapperName, dataField);
    }

  }

}
