#!/bin/sh

cd `dirname $0`/..

scripts/run-tool.sh com.opengamma.integration.tool.portfolio.PortfolioTemplateCreationTool $@ -c config/toolcontext/toolcontext-bloombergexample.properties -l com/opengamma/util/test/warn-logback.xml
