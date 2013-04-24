/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.CellMenu',
    dependencies: ['og.common.gadgets.mapping'],
    obj: function () {
        var module = this,
            icons = '.og-num, .og-icon-new-window-2',
            open_icon = '.og-small',
            expand_class = 'og-expanded',
            panels = ['south', 'dock-north', 'dock-center', 'dock-south'],
            width,
            mapping = og.common.gadgets.mapping;
        var constructor = function (grid) {
            var cellmenu = this, depgraph = grid.source.depgraph, primitives = grid.source.type === 'primitives',
                parent = grid.elements.parent, inplace_config, timer, singlepanel = og.analytics.containers.initialize;
            cellmenu.frozen = false;
            cellmenu.grid = grid;
            width = singlepanel ? 20 : 34;
            cellmenu.busy = (function (busy) {
                return function (value) {return busy = typeof value !== 'undefined' ? value : busy;};
            })(false);
            og.api.text({module: 'og.analytics.cell_options'}).pipe(function (template) {
                template = (Handlebars.compile(template))({singlepanel:singlepanel ? true : false});
                (cellmenu.menu = $(template)).hide()
                .on('mouseleave', function () {
                    clearTimeout(timer), cellmenu.menu.removeClass(expand_class), cellmenu.hide();
                })
                .on('mouseenter', open_icon, function () {
                    clearTimeout(timer), timer = setTimeout(function () {cellmenu.menu.addClass(expand_class);}, 500);
                })
                .on('mouseenter', function () {$.data(cellmenu, 'hover', true);})
                .on('click', open_icon, function () {
                    cellmenu.destroy_frozen();
                    cellmenu.menu.addClass(expand_class);
                })
                .on('mouseenter', icons, function () {
                    var panel = panels[$(this).text() - 1];
                    panels.forEach(function (val) {og.analytics.containers[val].highlight(true, val === panel);});
                })
                .on('mouseleave', icons, function () {
                    panels.forEach(function (val) {og.analytics.containers[val].highlight(false);});
                })
                .on('click', icons, function () {
                    var panel = panels[+$(this).text() - 1], cell = cellmenu.current,
                        options = mapping.options(cell, grid, panel);
                    cellmenu.destroy_frozen();
                    cellmenu.hide();
                    if (!panel) og.analytics.url.launch(options); 
                    else og.analytics.url.add(panel, options);
                });
                grid.on('cellhoverin', function (cell) {
                    if (cellmenu.frozen || cellmenu.busy()) return;
                    var type = cell.type, hide;
                    cellmenu.menu.removeClass(expand_class);
                    clearTimeout(timer);
                    cellmenu.current = cell;
                    hide =
                        // Hide the cell menu if...
                        !cell.value.logLevel                                    // always show if log exists
                        && ((cell.col === ((!primitives && !depgraph)           // Second column fixed column
                            && (grid.meta.columns.fixed.length === 2)) && (cell.col < 2))
                        || ((depgraph || primitives) && cell.col < 1)           // 1st column of depgraph or primitives
                        || (type === 'NODE')                                    // Is node
                        || (grid.source.blotter && cell.col > 0)                // All blotter cols other than first
                        || (cell.right > parent.width())                        // End of the cell not visible
                        || (depgraph && ~mapping.depgraph_blacklist.indexOf(type))); // Unsupported type on depgraph
                    if (hide) cellmenu.hide(); else cellmenu.show();
                })
                .on('cellhoverout', function () {
                    clearTimeout(timer);
                    setTimeout(function () {if (!cellmenu.menu.is(':hover')) {cellmenu.hide();}});
                })
                .on('scrollstart', function () {
                    cellmenu.busy(true);
                    if (cellmenu.frozen) cellmenu.destroy_frozen(); else cellmenu.hide();
                })
                .on('scrollend', function () {cellmenu.busy(false);});
                og.api.text({module: 'og.analytics.inplace_tash'}).pipe(function (tmpl_inplace) {
                    var unique = og.common.id('inplace');
                    inplace_config = {cntr: $('.og-inplace', cellmenu.menu), tmpl: tmpl_inplace, data: {name: unique}};
                    cellmenu.inplace = new og.common.util.ui.DropMenu(inplace_config);
                    cellmenu.container = new og.common.gadgets.GadgetsContainer('.OG-layout-analytics-', unique);
                    cellmenu.inplace.$dom.toggle.on('click', function () {
                        if (cellmenu.inplace.toggle_handler()) {
                            cellmenu.create_inplace('.OG-layout-analytics-' + unique);
                            cellmenu.inplace.$dom.menu.blurkill(cellmenu.destroy_frozen.bind(cellmenu));
                        }
                        else cellmenu.destroy_frozen();
                    });
                    cellmenu.container.on('del', function () {cellmenu.destroy_frozen();});
                });
            });
        };
        constructor.prototype.destroy_frozen = function () {
           $('.OG-cell-options.og-frozen').remove();
           $('.og-inplace-resizer').remove();
           og.common.gadgets.manager.clean();
        };
        constructor.prototype.create_inplace = function (unique) {
            var cellmenu = this, panel = 'inplace', options, cell = cellmenu.current,
                offset = cellmenu.inplace.$dom.cntr.offset(), inner = cellmenu.inplace.$dom.menu;
            cellmenu.destroy_frozen();
            cellmenu.frozen = true;
            cellmenu.menu.addClass('og-frozen');
            options = mapping.options(cell, cellmenu.grid, panel);
            cellmenu.container.add([options]);
            cellmenu.container.on('launch', og.analytics.url.launch);
            if ((offset.top + inner.height()) > $(window).height())
                inner.css({marginTop: -inner.outerHeight(true)-9});
            if ((offset.left + inner.width()) > $(window).width())
                inner.css({marginLeft: -inner.width() + width - cellmenu.menu.left} );
            new constructor(cellmenu.grid);
            og.analytics.resize({
                selector: unique,
                tmpl: '<div class="OG-analytics-resize og-resizer og-inplace-resizer" title="Drag to resize me" />',
                mouseup_handler: function (right, bottom) {
                    var newWidth = Math.max(480,($(document).outerWidth() - right) - inner.offset().left),
                        newHeight = ($(document).outerHeight() - bottom) - inner.offset().top;
                    inner.css({width: newWidth, height: newHeight});
                    cellmenu.container.resize();
                }
            });
        };
        constructor.prototype.hide = function () {
           var cellmenu = this;
            if (cellmenu.menu && cellmenu.menu.length && !cellmenu.frozen) {
                cellmenu.menu.hide();
            }
        };
        constructor.prototype.show = function () {
            var cellmenu = this, current = this.current;
            if (cellmenu.menu && cellmenu.menu.length) {
                cellmenu.menu
                    .appendTo($('body'))
                    .css({top: current.top, left: current.right - width + cellmenu.grid.offset.left}).show();
            }

        };
        return constructor;
    }
});
