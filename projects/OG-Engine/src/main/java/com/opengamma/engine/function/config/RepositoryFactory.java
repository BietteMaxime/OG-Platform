/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.MarketDataAliasingFunction;
import com.opengamma.engine.function.NoOpFunction;
import com.opengamma.util.ReflectionUtils;

/**
 * Constructs and bootstraps an {@link InMemoryFunctionRepository} based on configuration provided in a Fudge-encoded stream.
 */
public class RepositoryFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(RepositoryFactory.class);

  /**
   * The number of functions that are always in a constructed repository regardless of the {@link RepositoryConfiguration} document used. For example:
   * <ul>
   * <li>The no-op function used for execution suppression ({@link NoOpFunction})</li>
   * <li>The value aliasing function ({@link MarketDataAliasingFunction})</li>
   * </ul>
   * For exam
   */
  public static final int INTRINSIC_FUNCTION_COUNT = 2;

  /**
   * Constructs a repository from the configuration.
   * 
   * @param configuration the configuration, not null
   * @return the repository, not null
   */
  public static InMemoryFunctionRepository constructRepository(final RepositoryConfiguration configuration) {
    final InMemoryFunctionRepository repository = new InMemoryFunctionRepository();
    repository.addFunction(NoOpFunction.INSTANCE);
    repository.addFunction(MarketDataAliasingFunction.INSTANCE);
    if (configuration.getFunctions() != null) {
      for (final FunctionConfiguration functionConfig : configuration.getFunctions()) {
        if (functionConfig instanceof ParameterizedFunctionConfiguration) {
          addParameterizedFunctionConfiguration(repository, (ParameterizedFunctionConfiguration) functionConfig);
        } else if (functionConfig instanceof StaticFunctionConfiguration) {
          addStaticFunctionConfiguration(repository, (StaticFunctionConfiguration) functionConfig);
        } else {
          s_logger.error("Unhandled function configuration {}, ignoring", functionConfig);
        }
      }
    }
    return repository;
  }

  //-------------------------------------------------------------------------
  protected static void addParameterizedFunctionConfiguration(final InMemoryFunctionRepository repository, final ParameterizedFunctionConfiguration functionConfig) {
    try {
      final Class<?> definitionClass = ReflectionUtils.loadClass(functionConfig.getDefinitionClassName());
      final AbstractFunction functionDefinition = createParameterizedFunction(definitionClass, functionConfig.getParameter());
      repository.addFunction(functionDefinition);
    } catch (final RuntimeException ex) {
      s_logger.error("Unable to add function definition {}, ignoring", functionConfig);
      s_logger.info("Caught exception", ex);
    }
  }

  protected static AbstractFunction createParameterizedFunction(final Class<?> definitionClass, final List<String> parameterList) {
    try {
      constructors: for (final Constructor<?> constructor : definitionClass.getConstructors()) {
        final Class<?>[] parameters = constructor.getParameterTypes();
        final Object[] args = new Object[parameters.length];
        int used = 0;
        for (int i = 0; i < parameters.length; i++) {
          if (parameters[i] == String.class) {
            if (i < parameterList.size()) {
              args[i] = parameterList.get(i);
              used++;
            } else {
              continue constructors;
            }
          } else {
            if (i == parameters.length - 1) {
              used = parameterList.size();
              if (parameters[i] == String[].class) {
                args[i] = parameterList.subList(i, used).toArray(new String[used - i]);
              } else if (parameters[i].isAssignableFrom(List.class)) {
                args[i] = parameterList.subList(i, used);
              } else if (parameters[i].isAssignableFrom(Set.class)) {
                args[i] = new HashSet<String>(parameterList.subList(i, used));
              } else {
                continue constructors;
              }
            } else {
              continue constructors;
            }
          }
        }
        if (used != parameterList.size()) {
          continue;
        }
        return (AbstractFunction) constructor.newInstance(args);
      }
      throw new NoSuchMethodException("No suitable constructor found: " + definitionClass + ": " + parameterList);

    } catch (final RuntimeException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new OpenGammaRuntimeException("Unable to create static function: " + definitionClass + ": " + parameterList, ex);
    }
  }

  //-------------------------------------------------------------------------
  protected static void addStaticFunctionConfiguration(final InMemoryFunctionRepository repository, final StaticFunctionConfiguration functionConfig) {
    try {
      final Class<?> definitionClass = ReflectionUtils.loadClass(functionConfig.getDefinitionClassName());
      final AbstractFunction functionDefinition = createStaticFunction(definitionClass);
      repository.addFunction(functionDefinition);
    } catch (final RuntimeException ex) {
      s_logger.error("Unable to add function definition {}, ignoring", functionConfig);
      s_logger.info("Caught exception", ex);
    }
  }

  protected static AbstractFunction createStaticFunction(final Class<?> definitionClass) {
    try {
      return (AbstractFunction) definitionClass.newInstance();
    } catch (final RuntimeException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new OpenGammaRuntimeException("Unable to create static function: " + definitionClass, ex);
    }
  }

}
