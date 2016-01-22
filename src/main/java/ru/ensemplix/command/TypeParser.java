package ru.ensemplix.command;

/**
 * Реализация данного интерфейса позволяет конвертировать
 * строковый аргумент, отправленный игроком в нужный объект.
 */
public interface TypeParser<T> {

    /**
     * Конвертация строки в объект.
     *
     * @param value Строку, которую конвертируем в объект.
     * @return Объект, который получился после конвертации.
     */
    T parse(String value);

    class StringParser implements TypeParser<String> {
        @Override
        public String parse(String value) {
            return value;
        }
    }

    class IntegerParser implements TypeParser<Integer> {
        @Override
        public Integer parse(String value) {
            try {
                return Integer.parseInt(value);
            } catch(NumberFormatException e) {
                return 0;
            }
        }
    }

    class BooleanParser implements TypeParser<Boolean> {
        @Override
        public Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    }

    class FloatParser implements TypeParser<Float> {
        @Override
        public Float parse(String value) {
            try {
                return Float.parseFloat(value);
            } catch(NumberFormatException e) {
                return 0F;
            }
        }
    }

    class DoubleParser implements TypeParser<Double> {
        @Override
        public Double parse(String value) {
            try {
                return Double.parseDouble(value);
            } catch(NumberFormatException e) {
                return 0D;
            }
        }
    }

}
