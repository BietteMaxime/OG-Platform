/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.fxforwardsecurity',
    dependencies: [],
    obj: function () {
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, data;
            if(config.details) {data = config.details.data; data.id = config.details.data.trade.uniqueId;}
            else {data = {security: {type: "FXForwardSecurity", regionId: "ABC~123", externalIdBundle: "", 
                attributes: {}}, trade: og.blotter.util.otc_trade};}
            data.nodeId = config.portfolio.id;
            constructor.load = function () {
                constructor.title = 'FX Forward';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.fx_option_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {data.security.name = og.blotter.util.create_name(data);}
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form, counterparty: data.trade.counterparty, 
                        portfolio: data.nodeId, tradedate: data.trade.tradeDate}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.fx_forward_tash',
                        extras: {pay: data.security.payAmount, receive: data.security.receiveAmount},
                        children: [
                            new form.Block({module:'og.views.forms.currency_tash', 
                                extras:{name: 'security.payCurrency'}}),
                            new form.Block({module:'og.views.forms.currency_tash', 
                                extras:{name: 'security.receiveCurrency'}})
                        ]
                    }),                    
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    og.blotter.util.add_datetimepicker("security.forwardDate");
                    og.blotter.util.add_datetimepicker("trade.tradeDate");
                    if(data.security.length) return;
                    og.blotter.util.set_select("security.receiveCurrency", data.security.receiveCurrency);
                    og.blotter.util.set_select("security.payCurrency", data.security.payCurrency);
                    og.blotter.util.set_datetime("security.forwardDate", data.security.forwardDate);
                });
                form.on('form:submit', function (result){
                    config.handler(result.data);
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