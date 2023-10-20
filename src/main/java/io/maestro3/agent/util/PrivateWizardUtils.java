/*
 * Copyright 2023 Maestro Cloud Control LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.maestro3.agent.util;

import io.maestro3.sdk.internal.util.CollectionUtils;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStep;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateStepData;
import io.maestro3.sdk.v3.model.agent.wizard.SdkPrivateWizard;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkMessageItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkOptionItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkSelectItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableHeaderItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableHeaderType;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTableRowItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTextAreaItem;
import io.maestro3.sdk.v3.model.agent.wizard.item.SdkTextItem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PrivateWizardUtils {
    public static SdkMessageItem messageInfoItem(String name, String value, String order) {
        return messageItem(name, value, "INFO", order);
    }

    public static SdkMessageItem messageWarningItem(String name, String value, String order) {
        return messageItem(name, value, "WARNING", order);

    }

    public static SdkTextItem textItem(String name, String title, String placeholder, String description, String order) {
        return SdkTextItem.builder()
            .withName(name)
            .withTitle(title)
            .withDescription(description)
            .withRequired(true)
            .withOrder(order)
            .withPlaceholder(placeholder)
            .build();
    }

    public static SdkTextItem textItem(String name, String title, String value, String placeholder, String description, String order) {
        return SdkTextItem.builder()
            .withName(name)
            .withTitle(title)
            .withValue(value)
            .withDescription(description)
            .withRequired(true)
            .withOrder(order)
            .withPlaceholder(placeholder)
            .build();
    }

    public static SdkTextItem hiddenTextItem(String name, String value, String order) {
        return SdkTextItem.builder()
            .withName(name)
            .withValue(value)
            .withTitle(name)
            .withDescription(name)
            .withOrder(order)
            .withVisible(false)
            .build();
    }

    public static SdkTextItem securedTextItem(String name, String title, String placeholder, String description, String order) {
        return SdkTextItem.builder()
            .withName(name)
            .withTitle(title)
            .withType("password")
            .withDescription(description)
            .withRequired(true)
            .withOrder(order)
            .withPlaceholder(placeholder)
            .build();
    }

    public static SdkTextItem securedTextItem(String name, String title, String placeholder, String description, String order, String regex, String clientError) {
        return SdkTextItem.builder()
            .withName(name)
            .withTitle(title)
            .withType("password")
            .withDescription(description)
            .withRequired(true)
            .withOrder(order)
            .withRegExp(regex)
            .withClientError(clientError)
            .withPlaceholder(placeholder)
            .build();
    }

    public static SdkTextItem securedTextItem(String name, String title, String value, String placeholder, String description, String order) {
        return SdkTextItem.builder()
            .withName(name)
            .withTitle(title)
            .withValue(value)
            .withType("password")
            .withDescription(description)
            .withOrder(order)
            .withRequired(true)
            .withPlaceholder(placeholder)
            .build();
    }

    public static SdkTextItem textItem(String name, String title, String placeholder, String description, String order, String regex, String clientError) {
        return SdkTextItem.builder()
            .withName(name)
            .withTitle(title)
            .withRegExp(regex)
            .withClientError(clientError)
            .withDescription(description)
            .withOrder(order)
            .withRequired(true)
            .withPlaceholder(placeholder)
            .build();
    }

    public static SdkTextAreaItem textAreaItem(String name, String placeholder, String description, String order) {
        return SdkTextAreaItem.builder()
            .withName(name)
            .withTitle(name)
            .withDescription(description)
            .withOrder(order)
            .withPlaceholder(placeholder)
            .build();
    }

    public static SdkSelectItem selectItem(String name, String title, String description, String order, List<SdkOptionItem> items) {
        return SdkSelectItem.builder()
            .withName(name)
            .withTitle(title)
            .withType("list")
            .withDescription(description)
            .withOrder(order)
            .withOptions(items)
            .withSelectedIndex(getAndFixSingleSelectedIndex(items))
            .build();
    }

    public static SdkOptionItem getOptionItem(String name, boolean selected) {
        return SdkOptionItem.builder()
            .withName(name)
            .withSelected(selected)
            .withTitle(name)
            .withValue(name)
            .build();
    }

    public static SdkOptionItem getOptionItem(String name, String value, boolean selected) {
        return SdkOptionItem.builder()
            .withName(name)
            .withSelected(selected)
            .withTitle(name)
            .withValue(value)
            .build();
    }

    public static SdkOptionItem getOptionItem(String name, String value, boolean selected, SdkSelectItem... nestedSelect) {
        return SdkOptionItem.builder()
            .withName(name)
            .withSelected(selected)
            .withTitle(name)
            .withValue(value)
            .withSelect(Arrays.asList(nestedSelect))
            .build();
    }

    public static SdkOptionItem getOptionItem(String name, boolean selected, SdkTextItem textItem) {
        return SdkOptionItem.builder()
            .withName(name)
            .withSelected(selected)
            .withTitle(name)
            .withValue(name)
            .withText(Collections.singletonList(textItem))
            .build();
    }

    public static SdkTextItem getTextItem(SdkPrivateStep privateStep, String name) {
        SdkPrivateStepData stepData = privateStep.getData();
        List<SdkTextItem> texts = stepData.getText();
        for (SdkTextItem text : texts) {
            if (text.getName().equals(name)) {
                return text;
            }
        }
        throw new IllegalArgumentException(String.format("Text item with name %s is not found", name));
    }

    public static String getTextValueWithValidation(SdkPrivateStep privateStep, String name, Consumer<String> validator) {
        String textValue = getTextItem(privateStep, name).getValue();
        if (validator != null) {
            validator.accept(textValue);
        }
        return textValue;
    }

    public static List<SdkTableRowItem> getSelectedRowForTable(SdkPrivateStep privateStep, String name) {
        List<SdkTableItem> tables = privateStep.getData().getTable();
        SdkTableItem table = findItem(tables, name, SdkTableItem::getName);
        return getSelectedRowForTable(table);
    }

    public static List<SdkTableRowItem> getSelectedRowForTable(SdkTableItem table) {
        List<SdkTableHeaderItem> headers = table.getHeaders();
        int cellIndex = 0;
        for (SdkTableHeaderItem header : headers) {
            if (header.getType() != SdkTableHeaderType.CHECKBOX) {
                cellIndex++;
            } else {
                break;
            }
        }
        if (cellIndex == headers.size()) {
            throw new IllegalArgumentException("Incorrect table type. Failed to find cells with select type.");
        }
        int finalCellIndex = cellIndex;
        return table.getRow().stream()
            .filter(row -> "true".equals(row.getCell().get(finalCellIndex).getValue()))
            .collect(Collectors.toList());
    }

    public static String getTextValue(SdkPrivateStep privateStep, String name) {
        return getTextValueWithValidation(privateStep, name, null);
    }

    public static SdkOptionItem getSelectedOptionItem(SdkPrivateStep step, String selectName) {
        return getSelectedOptionItem(step.getData().getSelect(), selectName);
    }

    public static SdkOptionItem getSelectedOptionItem(List<SdkSelectItem> selectItems, String selectName) {
        List<SdkOptionItem> options = selectItems.stream()
            .filter(item -> item.getName().equals(selectName))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Can`t find select item by name " + selectName))
            .getOption();
        if (options.size() == 1){
            return options.get(0);
        }
        return options.stream()
            .filter(SdkOptionItem::getSelected)
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Can`t find selected option in select item " + selectName));
    }

    public static SdkOptionItem getSelectedOptionItem(List<SdkOptionItem> options) {
        if (options.size() == 1){
            return options.get(0);
        }
        return options.stream()
            .filter(SdkOptionItem::getSelected)
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Can`t find selected option"));
    }

    public static SdkSelectItem getSelectItem(SdkPrivateStep privateStep, String selectName) {
        return privateStep.getData().getSelect().stream()
            .filter(item -> item.getName().equals(selectName))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Can`t find select item by name " + selectName));
    }

    public static SdkPrivateStep getStepById(int stepId, List<SdkPrivateStep> steps) {
        return steps.stream()
            .filter(item -> item.getId().equals(stepId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Can`t find step by id " + stepId));
    }

    public static <ITEM, KEY> ITEM findItem(List<ITEM> items, KEY testKey, Function<ITEM, KEY> extractor) {
        ITEM itemNullable = findItemNullable(items, testKey, extractor);
        if (itemNullable == null) {
            throw new NoSuchElementException("Can`t find item by key " + testKey);
        }
        return itemNullable;
    }

    public static <ITEM, KEY> ITEM findItemNullable(List<ITEM> items, KEY testKey, Function<ITEM, KEY> extractor) {
        for (ITEM item : items) {
            if (extractor.apply(item).equals(testKey)) {
                return item;
            }
        }
        return null;
    }

    public static int getAndFixSingleSelectedIndex(List<SdkOptionItem> options) {
        int selectedIndex = 0;
        if (CollectionUtils.isNotEmpty(options)) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).getSelected()) {
                    selectedIndex = i;
                    break;
                }
            }
            options.forEach(option -> option.setSelected(false));
            options.get(selectedIndex).setSelected(true);
        }
        return selectedIndex;
    }

    public static void clearErrors(SdkPrivateWizard wizard, int stepId) {
        SdkPrivateStepData stepData = getStepById(stepId, wizard.getStep()).getData();

        List<SdkSelectItem> selectItems = stepData.getSelect();
        if (selectItems != null) {
            selectItems.forEach(item -> item.setServerError(null));
        }

        List<SdkTableItem> tables = stepData.getTable();
        if (tables != null) {
            tables.forEach(item -> item.setServerError(null));
        }

        List<SdkTextItem> textItems = stepData.getText();
        if (textItems != null) {
            textItems.forEach(item -> item.setServerError(null));
        }

        List<SdkTextAreaItem> textAreaItems = stepData.getTextArea();
        if (textAreaItems != null) {
            textAreaItems.forEach(item -> item.setServerError(null));
        }

        List<SdkMessageItem> messages = stepData.getMessage();
        if (messages != null) {
            messages.removeIf(messageItem -> messageItem.getName().contains("error"));
        }

        wizard.setValid(true);
    }

    private static SdkMessageItem messageItem(String name, String value, String type, String order) {
        return SdkMessageItem.builder()
            .withName(name)
            .withType(type)
            .withValue(value)
            .withOrder(order)
            .build();
    }


    private PrivateWizardUtils() {
    }
}
