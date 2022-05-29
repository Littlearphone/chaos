package com.littlearphone.arco.converter.printer;

import dnl.utils.text.table.SeparatorPolicy;
import dnl.utils.text.table.TextTable;

import javax.swing.RowSorter;
import java.io.PrintStream;
import java.util.List;

public class ExtendTablePrinter extends TextTable {
    public ExtendTablePrinter(String[] columnNames, Object[][] data) {
        super(columnNames, data);
    }
    
    @Override
    public void printTable(PrintStream ps, int indent) {
        new ExtendTableRenderer(this).render(ps, indent);
    }
    
    public boolean isAddRowNumbering() {
        return addRowNumbering;
    }
    
    public RowSorter<?> getRowSorter() {
        return rowSorter;
    }
    
    public List<SeparatorPolicy> getSeparatorPolicies() {
        return separatorPolicies;
    }
    
    public boolean hasSeparatorAt(int row) {
        return super.hasSeparatorAt(row);
    }
}
