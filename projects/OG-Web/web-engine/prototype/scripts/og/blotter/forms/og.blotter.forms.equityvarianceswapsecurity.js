/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.equityvarianceswapsecurity',
    dependencies: [],
    obj: function () {   
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data;
            if(config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {security: {type: "EquityVarianceSwapSecurity", name: "EquityVarianceSwapSecurity ABC", 
                regionId: "ABC~123", externalIdBundle: ""}, trade: og.blotter.util.otc_trade};}
            data.portfolio = config.portfolio;
            constructor.load = function () {
                constructor.title = 'Equity Varience Swap';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.simple_tash',
                    selector: '.OG-blotter-form-block',
                    data: data
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty, 
                        portfolio: data.portfolio.name}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.equity_variance_swap_tash',
                        extras: {notional: data.security.notional, region: data.security.regionId,
                            settlement: data.security.settlementDate, strike: data.security.strike,
                            first: data.security.firstObservationDate, last: data.security.lastObservationDate,
                            annualization: data.security.annualizationFactor
                         },
                        processor: function (data) {
                            data.security.parameterizedAsVariance = 
                            og.blotter.util.get_checkbox("security.parameterizedAsVariance");
                        },
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash',
                                extras:{name: "security.currency"}}),
                            new og.blotter.forms.blocks.Security({
                                form: form, label: "Spot Underlying ID", security: data.security.spotUnderlyingId,
                                index: "security.spotUnderlyingId"
                            }), 
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: 'security.observationFrequency',
                                value: data.security.observationFrequency, placeholder: 'Frequency'
                            })                               
                        ]
                    }),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    og.blotter.util.add_datetimepicker("security.settlementDate");
                    og.blotter.util.add_datetimepicker("security.lastObservationDate");
                    og.blotter.util.add_datetimepicker("security.firstObservationDate");
                    if(data.security.length) return;
                    og.blotter.util.set_select("security.currency", data.security.currency);
                    og.blotter.util.check_checkbox("security.parameterizedAsVariance", 
                        data.security.parameterizedAsVariance);
                });
                form.on('form:submit', function (result){
                    og.api.rest.blotter.trades.put(result.data);
                });
            }; 
            constructor.load();
            constructor.submit = function () {
                form.submit();
            };
            constructor.submit_new = function () {
                delete data.id;
                form.submit();
            };
            constructor.kill = function () {
            };
        };
    }
});