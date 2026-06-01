package model;

import java.util.ArrayList;
import java.util.List;

public class FormulaEngine {
    private SheetList sheetList;

    public FormulaEngine(SheetList sheetList) {
        this.sheetList = sheetList;
    }

    public String evaluate(String raw, OrthoMatrix currentMatrix) {
        if (raw == null || raw.isEmpty()) return "";

        String trimmed = raw.trim();
        if (!trimmed.startsWith("=")) return trimmed;

        String expr = trimmed.substring(1).trim();

        try {
            if (startsWithIgnoreCase(expr, "suma(")) {
                return String.valueOf(evalSum(expr.substring(5, expr.lastIndexOf(')')), currentMatrix));
            } else if (startsWithIgnoreCase(expr, "mult(")) {
                return String.valueOf(evalMult(expr.substring(5, expr.lastIndexOf(')')), currentMatrix));
            } else {
                return String.valueOf(evalArithmetic(expr, currentMatrix));
            }
        } catch (Exception e) {
            return "#ERR";
        }
    }

    private double evalSum(String args, OrthoMatrix current) {
        double total = 0;
        for (String part : splitArgs(args))
            total += resolveValue(part.trim(), current);
        return total;
    }

    private double evalMult(String args, OrthoMatrix current) {
        double result = 1;
        for (String part : splitArgs(args))
            result *= resolveValue(part.trim(), current);
        return result;
    }

    private double evalArithmetic(String expr, OrthoMatrix current) {
        // Simple left-to-right with +, -, *, /
        List<Double> nums = new ArrayList<>();
        List<Character> ops = new ArrayList<>();

        StringBuilder token = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char ch = expr.charAt(i);
            if ((ch == '+' || ch == '-' || ch == '*' || ch == '/') && token.length() > 0) {
                nums.add(resolveValue(token.toString().trim(), current));
                ops.add(ch);
                token.setLength(0);
            } else {
                token.append(ch);
            }
        }
        if (token.length() > 0)
            nums.add(resolveValue(token.toString().trim(), current));

        // First pass: * and /
        for (int i = 0; i < ops.size(); ) {
            char op = ops.get(i);
            if (op == '*' || op == '/') {
                double left = nums.get(i);
                double right = nums.get(i + 1);
                double res = op == '*' ? left * right : left / right;
                nums.set(i, res);
                nums.remove(i + 1);
                ops.remove(i);
            } else {
                i++;
            }
        }

        // Second pass: + and -
        double result = nums.get(0);
        for (int i = 0; i < ops.size(); i++) {
            char op = ops.get(i);
            double next = nums.get(i + 1);
            result = op == '+' ? result + next : result - next;
        }
        return result;
    }

    private double resolveValue(String token, OrthoMatrix current) {
        if (token.isEmpty()) return 0;

        // Cross-sheet: Hoja1,(2,5) or Hoja 1,(2,5)
        if (startsWithIgnoreCase(token, "hoja")) {
            return resolveCrossSheet(token);
        }

        // Cell reference like A1, B5
        if (Character.isLetter(token.charAt(0)) && token.length() >= 2) {
            int col = token.charAt(0) - 'A';
            try {
                int row = Integer.parseInt(token.substring(1)) - 1;
                String val = current.getDisplayValue(row, col);
                return val.isEmpty() ? 0 : Double.parseDouble(val);
            } catch (NumberFormatException ignored) {}
        }

        return Double.parseDouble(token);
    }

    private double resolveCrossSheet(String token) {
        // Format: Hoja1,(row,col) — 1-indexed
        try {
            int parenStart = token.indexOf('(');
            int parenEnd = token.indexOf(')');
            String sheetPart = token.substring(0, parenStart).trim();
            int sheetNum = Integer.parseInt(sheetPart.replaceAll("[^0-9]", "")) - 1;

            String coordPart = token.substring(parenStart + 1, parenEnd);
            String[] coords = coordPart.split(",");
            int row = Integer.parseInt(coords[0].trim()) - 1;
            int col = Integer.parseInt(coords[1].trim()) - 1;

            Sheet target = sheetList.getSheet(sheetNum);
            if (target == null) return 0;

            String val = target.getMatrix().getDisplayValue(row, col);
            return val.isEmpty() ? 0 : Double.parseDouble(val);
        } catch (Exception e) {
            return 0;
        }
    }

    private List<String> splitArgs(String args) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (char ch : args.toCharArray()) {
            if (ch == '(') depth++;
            else if (ch == ')') depth--;
            if (ch == ',' && depth == 0) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        if (cur.length() > 0) parts.add(cur.toString());
        return parts;
    }

    private boolean startsWithIgnoreCase(String s, String prefix) {
        return s.length() >= prefix.length() &&
               s.substring(0, prefix.length()).equalsIgnoreCase(prefix);
    }
}
