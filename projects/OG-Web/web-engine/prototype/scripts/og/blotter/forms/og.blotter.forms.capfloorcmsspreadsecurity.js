/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.capfloorcmsspreadsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data;
            if(config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {security: {type: "CapFloorCMSSpreadSecurity", name: "CapFloorCMSSpreadSecurity ABC", 
                regionId: "ABC~123", externalIdBundle: ""}, trade: og.blotter.util.otc_trade};}
            data.portfolio = config.portfolio;
            constructor.load = function () {
                constructor.title = 'Cap/Floor CMS Spread';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.simple_tash',
                    selector: '.OG-blotter-form-block',
                    data: data
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty, 
                        portfolio: data.portfolio.name}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.cap_floor_cms_tash',
                        extras: {start: data.security.startDate, maturity: data.security.maturityDate, 
                            notional: data.security.notional,strike: data.security.strike, longId: data.security.longId, 
                            shortId: data.security.shortId
                        },
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash',
                                extras:{name: "security.currency"}}),
                            new og.blotter.forms.blocks.Security({
                                form: form, label: "Long Underlying ID", security: data.security.longId,
                                index: "security.longId"
                            }),
                            new og.blotter.forms.blocks.Security({
                                form: form, label: "Short Underlying ID", security: data.security.shortId,
                                index: "security.shortId"
                            }),                            
                            new ui.Dropdown({
                                form: form, resource: 'blotter.frequencies', index: 'security.frequency',
                                value: data.security.frequency, placeholder: 'Select Frequency'
                            }),
                            new ui.Dropdown({
                                form: form, resource: 'blotter.daycountconventions', index: 'security.dayCount',
                                value: data.security.dayCount, placeholder: 'Select Day Count'
                            })
                        ]
                    }),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    og.blotter.util.add_datetimepicker("security.startDate");
                    og.blotter.util.add_datetimepicker("security.maturityDate");
                    if(data.security.length) return;
                    og.blotter.util.set_select("security.currency", data.security.currency);
                    og.blotter.util.check_radio("security.cap", data.security.cap);
                    og.blotter.util.check_radio("security.payer", data.security.payer);
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