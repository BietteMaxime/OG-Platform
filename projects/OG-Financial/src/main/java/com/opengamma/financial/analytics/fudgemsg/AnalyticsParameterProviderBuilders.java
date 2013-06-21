/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Period;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Contains builders for the objects that analytics needs to perform pricing.
 */
public final class AnalyticsParameterProviderBuilders {

  private AnalyticsParameterProviderBuilders() {
  }

  /**
   * Fudge builder for {@link IborIndex}
   */
  @FudgeBuilderFor(IborIndex.class)
  public static class IborIndexBuilder extends AbstractFudgeBuilder<IborIndex> {
    /** Currencies field */
    private static final String CURRENCY_FIELD = "currency";
    /** Spot lag field */
    private static final String SPOT_LAG_FIELD = "spotLag";
    /** Daycount field */
    private static final String DAY_COUNT_FIELD = "dayCount";
    /** Business day convention field */
    private static final String BUSINESS_DAY_CONVENTION_FIELD = "businessDayConvention";
    /** EOM convention field */
    private static final String EOM_FIELD = "isEOM";
    /** The tenor field */
    private static final String TENOR_FIELD = "tenor";
    /** The name field */
    private static final String NAME_FIELD = "name";

    @Override
    public IborIndex buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final int spotLag = message.getInt(SPOT_LAG_FIELD);
      final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(message.getString(DAY_COUNT_FIELD));
      final BusinessDayConvention businessDayConvention = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(message.getString(BUSINESS_DAY_CONVENTION_FIELD));
      final boolean isEOM = message.getBoolean(EOM_FIELD);
      final Period tenor = Period.parse(message.getString(TENOR_FIELD));
      final String name = message.getString(NAME_FIELD);
      return new IborIndex(currency, tenor, spotLag, dayCount, businessDayConvention, isEOM, name);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final IborIndex object) {
      message.add(SPOT_LAG_FIELD, object.getSpotLag());
      message.add(DAY_COUNT_FIELD, object.getDayCount().getConventionName());
      message.add(BUSINESS_DAY_CONVENTION_FIELD, object.getBusinessDayConvention().getConventionName());
      message.add(EOM_FIELD, object.isEndOfMonth());
      message.add(TENOR_FIELD, object.getTenor().toString());
      message.add(NAME_FIELD, object.getName());
      message.add(CURRENCY_FIELD, object.getCurrency().getCode());
    }
  }

  /**
   * Fudge builder for {@link IndexON}
   */
  @FudgeBuilderFor(IndexON.class)
  public static class IndexONBuilder extends AbstractFudgeBuilder<IndexON> {
    /** Currencies field */
    private static final String CURRENCY_FIELD = "currency";
    /** Index name field */
    private static final String NAME_FIELD = "name";
    /** Daycount field */
    private static final String DAY_COUNT_FIELD = "dayCount";
    /** Publication lag field */
    private static final String PUBLICATION_LAG_FIELD = "publicationLag";

    @Override
    public IndexON buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = message.getString(NAME_FIELD);
      final Currency currency = Currency.of(message.getString(CURRENCY_FIELD));
      final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount(message.getString(DAY_COUNT_FIELD));
      final int publicationLag = message.getInt(PUBLICATION_LAG_FIELD);
      return new IndexON(name, currency, dayCount, publicationLag);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final IndexON object) {
      message.add(CURRENCY_FIELD, object.getCurrency().getCode());
      message.add(NAME_FIELD, object.getName());
      message.add(DAY_COUNT_FIELD, object.getDayCount().getConventionName());
      message.add(PUBLICATION_LAG_FIELD, object.getPublicationLag());
    }

  }

  /**
   * Fudge builder for {@link FXMatrix}
   */
  @FudgeBuilderFor(FXMatrix.class)
  public static class FXMatrixBuilder extends AbstractFudgeBuilder<FXMatrix> {
    /** Currencies field */
    private static final String CURRENCY_FIELD = "currency";
    /** Order (of the entries) field */
    private static final String ORDER_FIELD = "order";
    /** Entries field */
    private static final String ENTRIES_FIELD = "entries";
    /** FX rates field */
    private static final String FX_RATES_FIELD = "fxRates";
    /** Row field */
    private static final String ROW_FIELD = "row";

    @Override
    public FXMatrix buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> currencies = message.getAllByName(CURRENCY_FIELD);
      final List<FudgeField> orders = message.getAllByName(ORDER_FIELD);
      final Map<Currency, Integer> map = new HashMap<>();
      for (int i = 0; i < currencies.size(); i++) {
        final Currency currency = Currency.of((String) currencies.get(i).getValue());
        final Integer order = ((Number) orders.get(i).getValue()).intValue();
        map.put(currency, order);
      }
      final List<FudgeField> entries = message.getAllByName(ENTRIES_FIELD);
      final List<FudgeField> arrays = message.getAllByName(FX_RATES_FIELD);
      final double[][] fxRates = new double[entries.size()][];
      for (int i = 0; i < entries.size(); i++) {
        final FudgeMsg msg = (FudgeMsg) arrays.get(i).getValue();
        final double[] row = deserializer.fieldValueToObject(double[].class, msg.getByName(ROW_FIELD));
        fxRates[i] = row;
      }
      return new FXMatrix(map, fxRates);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final FXMatrix object) {
      final Map<Currency, Integer> currencies = object.getCurrencies();
      for (final Map.Entry<Currency, Integer> entry : currencies.entrySet()) {
        message.add(CURRENCY_FIELD, entry.getKey().getCode());
        message.add(ORDER_FIELD, entry.getValue());
      }
      final double[][] rates = object.getRates();
      for (final double[] array : rates) {
        message.add(ENTRIES_FIELD, array.length);
        final MutableFudgeMsg msg = serializer.newMessage();
        serializer.addToMessageWithClassHeaders(msg, ROW_FIELD, null, array);
        message.add(FX_RATES_FIELD, msg);
      }
    }

  }

  /**
   * Fudge builder for {@link MulticurveProviderDiscount}
   */
  @FudgeBuilderFor(MulticurveProviderDiscount.class)
  public static class MulticurveProviderDiscountBuilder extends AbstractFudgeBuilder<MulticurveProviderDiscount> {
    /** Currencies field */
    private static final String CURRENCY_FIELD = "currency";
    /** Discounting curves field */
    private static final String DISCOUNTING_CURVE_FIELD = "discountingCurve";
    /** Overnight indices field */
    private static final String INDEX_ON_FIELD = "indexON";
    /** Overnight curves field */
    private static final String OVERNIGHT_CURVE_FIELD = "overnightCurve";
    /** Index indices field */
    private static final String INDEX_IBOR_FIELD = "iborIndex";
    /** Ibor curves field */
    private static final String INDEX_IBOR_CURVE = "iborCurve";
    /** FX matrix field */
    private static final String FX_MATRIX_FIELD = "fxMatrix";

    @Override
    public MulticurveProviderDiscount buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Map<Currency, YieldAndDiscountCurve> discountingCurves = new LinkedHashMap<>();
      final List<FudgeField> currencyFields = message.getAllByName(CURRENCY_FIELD);
      final List<FudgeField> discountingCurveFields = message.getAllByName(DISCOUNTING_CURVE_FIELD);
      for (int i = 0; i < currencyFields.size(); i++) {
        final Currency currency = Currency.of((String) currencyFields.get(i).getValue());
        final YieldAndDiscountCurve curve = deserializer.fudgeMsgToObject(YieldAndDiscountCurve.class, (FudgeMsg) discountingCurveFields.get(i).getValue());
        discountingCurves.put(currency, curve);
      }
      final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = new LinkedHashMap<>();
      final List<FudgeField> indexIborFields = message.getAllByName(INDEX_IBOR_FIELD);
      final List<FudgeField> forwardIborCurveFields = message.getAllByName(INDEX_IBOR_CURVE);
      for (int i = 0; i < currencyFields.size(); i++) {
        final IborIndex index = deserializer.fudgeMsgToObject(IborIndex.class, (FudgeMsg) indexIborFields.get(i).getValue());
        final YieldAndDiscountCurve curve = deserializer.fudgeMsgToObject(YieldAndDiscountCurve.class, (FudgeMsg) forwardIborCurveFields.get(i).getValue());
        forwardIborCurves.put(index, curve);
      }
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = new LinkedHashMap<>();
      final List<FudgeField> indexONFields = message.getAllByName(INDEX_ON_FIELD);
      final List<FudgeField> forwardONCurveFields = message.getAllByName(OVERNIGHT_CURVE_FIELD);
      for (int i = 0; i < currencyFields.size(); i++) {
        final IndexON index = deserializer.fudgeMsgToObject(IndexON.class, (FudgeMsg) indexONFields.get(i).getValue());
        final YieldAndDiscountCurve curve = deserializer.fudgeMsgToObject(YieldAndDiscountCurve.class, (FudgeMsg) forwardONCurveFields.get(i).getValue());
        forwardONCurves.put(index, curve);
      }
      final FXMatrix fxMatrix = deserializer.fieldValueToObject(FXMatrix.class, message.getByName(FX_MATRIX_FIELD));
      return new MulticurveProviderDiscount(discountingCurves, forwardIborCurves, forwardONCurves, fxMatrix);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final MulticurveProviderDiscount object) {
      final Map<Currency, YieldAndDiscountCurve> discountingCurves = object.getDiscountingCurves();
      for (final Map.Entry<Currency, YieldAndDiscountCurve> entry : discountingCurves.entrySet()) {
        message.add(CURRENCY_FIELD, entry.getKey().getCode());
        serializer.addToMessageWithClassHeaders(message, DISCOUNTING_CURVE_FIELD, null, entry.getValue());
      }
      final Map<IborIndex, YieldAndDiscountCurve> forwardIborCurves = object.getForwardIborCurves();
      for (final Map.Entry<IborIndex, YieldAndDiscountCurve> entry : forwardIborCurves.entrySet()) {
        serializer.addToMessageWithClassHeaders(message, INDEX_IBOR_FIELD, null, entry.getKey());
        serializer.addToMessageWithClassHeaders(message, INDEX_IBOR_CURVE, null, entry.getValue());
      }
      final Map<IndexON, YieldAndDiscountCurve> forwardONCurves = object.getForwardONCurves();
      for (final Map.Entry<IndexON, YieldAndDiscountCurve> entry : forwardONCurves.entrySet()) {
        serializer.addToMessageWithClassHeaders(message, INDEX_ON_FIELD, null, entry.getKey());
        serializer.addToMessageWithClassHeaders(message, OVERNIGHT_CURVE_FIELD, null, entry.getValue());
      }
      serializer.addToMessageWithClassHeaders(message, FX_MATRIX_FIELD, null, object.getFxRates());
    }

  }

  /**
   * Fudge builder for {@link CurveBuildingBlock}
   */
  @FudgeBuilderFor(CurveBuildingBlock.class)
  public static class CurveBuilderBlockBuilder extends AbstractFudgeBuilder<CurveBuildingBlock> {
    /** The curve names */
    private static final String CURVE_NAME_FIELD = "curve";
    /** The start index for a curve */
    private static final String START_INDEX_FIELD = "startIndex";
    /** The number of parameters for a curve */
    private static final String NUMBER_FIELD = "number";

    @Override
    public CurveBuildingBlock buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> curveNames = message.getAllByName(CURVE_NAME_FIELD);
      final List<FudgeField> startIndices = message.getAllByName(START_INDEX_FIELD);
      final int n = curveNames.size();
      if (startIndices.size() != n) {
        throw new IllegalStateException("Should have one start index for each curve name; have " + curveNames + " and " + startIndices);
      }
      final List<FudgeField> numbers = message.getAllByName(NUMBER_FIELD);
      if (numbers.size() != n) {
        throw new IllegalStateException("Should have one parameter number for each curve name; have " + curveNames + " and " + numbers);
      }
      final LinkedHashMap<String, Pair<Integer, Integer>> data = new LinkedHashMap<>();
      for (int i = 0; i < n; i++) {
        final String curveName = (String) curveNames.get(i).getValue();
        final Integer startIndex = ((Number) startIndices.get(i).getValue()).intValue();
        final Integer number = ((Number) numbers.get(i).getValue()).intValue();
        data.put(curveName, Pair.of(startIndex, number));
      }
      return new CurveBuildingBlock(data);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final CurveBuildingBlock object) {
      final Map<String, Pair<Integer, Integer>> data = object.getData();
      for (final Map.Entry<String, Pair<Integer, Integer>> entry : data.entrySet()) {
        message.add(CURVE_NAME_FIELD, entry.getKey());
        message.add(START_INDEX_FIELD, entry.getValue().getFirst());
        message.add(NUMBER_FIELD, entry.getValue().getSecond());
      }
    }

  }

  /**
   * Fudge builder for {@link CurveBuildingBlockBundle}
   */
  @FudgeBuilderFor(CurveBuildingBlockBundle.class)
  public static class CurveBuildingBlockBundleBuilder extends AbstractFudgeBuilder<CurveBuildingBlockBundle> {
    /** Curve name field */
    private static final String CURVE_NAME_FIELD = "curve";
    /** Block name field */
    private static final String BLOCK_FIELD = "block";
    /** Matrix field */
    private static final String MATRIX_FIELD = "matrix";

    @Override
    public CurveBuildingBlockBundle buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<FudgeField> curveNames = message.getAllByName(CURVE_NAME_FIELD);
      final List<FudgeField> blocks = message.getAllByName(BLOCK_FIELD);
      final List<FudgeField> matrices = message.getAllByName(MATRIX_FIELD);
      final int n = curveNames.size();
      if (blocks.size() != n) {
        throw new IllegalStateException("Should have one block for each curve name; have " + curveNames + " and " + blocks);
      }
      if (matrices.size() != n) {
        throw new IllegalStateException("Should have one matrix for each curve name; have " + curveNames + " and " + matrices);
      }
      final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> data = new LinkedHashMap<>();
      for (int i = 0; i < n; i++) {
        final String curveName = (String) curveNames.get(i).getValue();
        final CurveBuildingBlock block = deserializer.fieldValueToObject(CurveBuildingBlock.class, blocks.get(i));
        final DoubleMatrix2D m = new DoubleMatrix2D(deserializer.fieldValueToObject(double[][].class, matrices.get(i)));
        data.put(curveName, Pair.of(block, m));
      }
      return new CurveBuildingBlockBundle(data);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final CurveBuildingBlockBundle object) {
      final Map<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> data = object.getData();
      for (final Map.Entry<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> entry : data.entrySet()) {
        message.add(CURVE_NAME_FIELD, entry.getKey());
        serializer.addToMessageWithClassHeaders(message, BLOCK_FIELD, null, entry.getValue().getFirst());
        serializer.addToMessageWithClassHeaders(message, MATRIX_FIELD, null, entry.getValue().getSecond().getData());
      }
    }

  }
}
