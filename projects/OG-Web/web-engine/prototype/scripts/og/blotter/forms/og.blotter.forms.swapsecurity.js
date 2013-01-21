/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.swapsecurity',
    dependencies: [],
    obj: function () {   
        return function (config) {
            var constructor = this, form, ui = og.common.util.ui, pay_block, receive_block, pay_select, receive_select,
                pay_index = og.common.id('pay'), receive_index = og.common.id('receive'), pay_leg = 'security.payLeg.', 
                receive_leg = 'security.receiveLeg.', $pay_select, $receive_select;
            if(config) {data = config; data.id = config.trade.uniqueId;}
            else {data = {security: {type: "SwapSecurity", name: "SwapSecurity ABC", 
                regionId: "ABC~123", externalIdBundle: ""}, trade: og.blotter.util.otc_trade};} 
            constructor.load = function () {
                constructor.title = 'Swap';
                form = new og.common.util.ui.Form({
                    module: 'og.blotter.forms.swap_tash',
                    selector: '.OG-blotter-form-block',
                    data: data,
                    processor: function (data) {
                        data.security.payLeg.type = $pay_select.val();
                        data.security.receiveLeg.type = $receive_select.val();
                        data.security.counterparty = data.trade.counterparty;
                        data.security.attributes = {};
                        data.security.payLeg.regionId = data.security.regionId;
                        data.security.receiveLeg.regionId = data.security.regionId;
                        data.security.payLeg.notional.type = 'InterestRateNotional';
                        data.security.receiveLeg.notional.type = 'InterestRateNotional';
                    }
                });
                form.children.push(
                    new og.blotter.forms.blocks.Portfolio({form: form}),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_quick_entry_tash'
                    }),
                    new form.Block({
                        module: 'og.blotter.forms.blocks.swap_details_tash',
                        extras: {trade: data.security.tradeDate, maturity: data.security.maturityDate, 
                            effective: data.security.effectiveDate}
                    }),
                    pay_select = new ui.Dropdown({
                        form: form, placeholder: 'Select Swap Type',
                        data_generator: function (handler) {handler(og.blotter.util.swap_types);}
                    }),
                    pay_block = new form.Block({content:"<div id='" + pay_index + "'></div>"}),
                    receive_select = new ui.Dropdown({
                        form: form, placeholder: 'Select Swap Type',
                        data_generator: function (handler) {handler(og.blotter.util.swap_types);}
                    }),
                    receive_block = new form.Block({content:"<div id='" + receive_index + "'></div>"}),
                    new og.common.util.ui.Attributes({
                        form: form, attributes: data.trade.attributes, index: 'trade.attributes'
                    })
                );
                form.dom();
                form.on('form:load', function (){
                    $pay_select = $('#' + pay_select.id);
                    $receive_select = $('#' + receive_select.id);
                    og.blotter.util.add_datetimepicker("security.tradeDate");
                    og.blotter.util.add_datetimepicker("security.effectiveDate");
                    og.blotter.util.add_datetimepicker("security.maturityDate");
                    if(typeof data.security.payLeg != 'undefined') {
                        swap_leg({type: data.security.payLeg.type, index: pay_index, leg: pay_leg, child: 4});
                        $pay_select.val(data.security.payLeg.type);
                    }   
                    if(typeof data.security.receiveLeg != 'undefined'){
                        swap_leg({type: data.security.receiveLeg.type, index: receive_index,leg: receive_leg,child: 6});
                        $receive_select.val(data.security.receiveLeg.type);
                    }
                }); 
                form.on('form:submit', function (result){
                    og.api.rest.blotter.trades.put(result.data);
                });
                form.on('change', '#' + pay_select.id, function (event) {
                    swap_leg({type: event.target.value, index: pay_index, leg: pay_leg, child: 4});
                    og.blotter.util.toggle_fixed($receive_select, event.target.value);

                });
                form.on('change', '#' + receive_select.id,  function (event) {
                    swap_leg({type: event.target.value, index: receive_index, leg: receive_leg, child: 6});
                    og.blotter.util.toggle_fixed($pay_select, event.target.value);
                });
            };
            swap_leg = function (swap) {
                var new_block;
                if(!swap.type.length) {new_block = new form.Block({content:"<div id='" + swap.index + "'></div>"});}
                else if(!~swap.type.indexOf('Floating')){
                    new_block = new og.blotter.forms.blocks.Fixedleg({
                        form: form, data: data, leg: swap.leg, index: swap.index
                    });
                }
                else {
                    new_block = new og.blotter.forms.blocks.Floatingleg({
                        form: form, data: data, leg: swap.leg, type: swap.type, index: swap.index
                    });                    
                }
                new_block.html(function (html) {
                    $('#' + swap.index).replaceWith(html);
                    if(typeof data.security.recieveLeg != 'undefined')
                        og.blotter.util.check_checkbox(receive_leg + 'eom', data.security.receiveLeg.eom);
                    if(typeof data.security.payLeg != 'undefined')
                        og.blotter.util.check_checkbox(pay_leg + 'eom', data.security.payLeg.eom);

                });
                form.children[swap.child] = new_block;
                

            };
            constructor.load();
            constructor.submit = function () {
                form.submit();
            };
            constructor.kill = function () {
            };
        };
    }
});