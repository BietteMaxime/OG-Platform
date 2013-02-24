/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.capfloorsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data, validate;
            if(config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {security: {type: "CapFloorSecurity", externalIdBundle: "", attributes: {}}, 
                trade: og.blotter.util.otc_trade};}
            data.nodeId = config.portfolio.id;
            constructor.load = function () {
                constructor.title = 'Cap/Floor';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.simple_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {data.security.name = og.blotter.util.create_name(data);}
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty,
                        portfolio: data.nodeId, trade: data.trade}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.cap_floor_tash',
                        extras: {start: data.security.startDate, maturity: data.security.maturityDate,
                            notional: data.security.notional,strike: data.security.strike,
                            underlyingId: data.security.underlyingId
                        },
                        processor: function (data) {
                            data.security.ibor = og.blotter.util.get_checkbox("security.ibor");
                        },
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash',
                                extras:{name: "security.currency"}}),
                            new og.blotter.forms.blocks.Security({
                                form: form, label: "Underlying ID", security: data.security.underlyingId,
                                index: "security.underlyingId"
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
                    og.blotter.util.add_date_picker('.blotter-date');
                    og.blotter.util.add_time_picker('.blotter-time');
                    if(data.security.length) return;
                    og.blotter.util.set_select("security.currency", data.security.currency);
                    og.blotter.util.check_radio("security.cap", data.security.cap);
                    og.blotter.util.check_radio("security.payer", data.security.payer);
                    og.blotter.util.check_checkbox("security.ibor", data.security.ibor);
                });
                form.on('form:submit', function (result){
                    $.when(config.handler(result.data)).then(validate);
                });
            };
            constructor.load();
            constructor.submit = function (handler) {
                validate = handler;
                form.submit();
            };
            constructor.submit_new = function (handler) {
                validate = handler;
                delete data.id;
                form.submit();
            };
        };
    }
});