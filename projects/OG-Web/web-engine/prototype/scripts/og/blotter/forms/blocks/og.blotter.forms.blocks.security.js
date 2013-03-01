/**
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.Security',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Security = function (config) {
            var block = this, scheme_value, id_value, form = config.form, security = config.security, dropdown,
            sec_id = og.common.id("og-blotter-security"), scheme_id = og.common.id("og-blotter-scheme");
            if(security){
                scheme_value = security.split(/~(.+)/)[0];
                id_value  = security.split(/~(.+)/)[1]; 
            }
            form.Block.call(block, {
                module: 'og.blotter.forms.blocks.security_tash', 
                extras: {label: config.label, sec_id: sec_id, value: id_value, disabled: !!config.edit},
                children: [
                    dropdown = new og.common.util.ui.Dropdown({
                        form: form, resource: 'blotter.idschemes', index: scheme_id,
                        value: scheme_value, placeholder: 'Select Scheme', disabled: !!config.edit,
                        data_generator: function (handler) {
                            og.api.rest.blotter.idschemes.get().pipe(function (result){
                                var options = [], obj = result.data;
                                Object.keys(obj).forEach(function(key) { 
                                    options.push(obj[key]);
                                });
                                handler(options);
                            });
                        }
                    })
                ],                       
                processor: function (data) {
                    if (!config.edit) {
                        var path = config.index.split('.'), last = path.pop(), 
                            merge = data[scheme_id] + "~" + data[sec_id];
                        path.reduce(function (acc, val) {return acc[val];}, data)[last] = merge;                        
                    }
                    delete data[sec_id];
                    delete data[scheme_id];
                }
            });
            block.name = function () {
                var scheme = $(this.select_id()).val().trim(), id = $(this.input_id()).val().trim();
                if (!scheme.length || !id.length) return false;
                return  scheme + '~' + id;
            };
            block.input_id = function () {
                return '#' + sec_id;
            };
            block.select_id = function () {
                return '#' + dropdown.id;
            };
        };
        Security.prototype = new Block(); // inherit Block prototype
        return Security;
    }
});