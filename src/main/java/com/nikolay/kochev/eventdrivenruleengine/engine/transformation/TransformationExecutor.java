package com.nikolay.kochev.eventdrivenruleengine.engine.transformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.nikolay.kochev.eventdrivenruleengine.engine.enums.TransformOperation;
import com.nikolay.kochev.eventdrivenruleengine.engine.loader.TransformationLoader;
import com.nikolay.kochev.eventdrivenruleengine.engine.transformation.operations.*;
import com.nikolay.kochev.eventdrivenruleengine.exception.TransformationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;


@Service
public class TransformationExecutor {

    private final Map<TransformOperation, TransformationOperation> operations;

    @Autowired
    public TransformationExecutor(
            SetOperation setOperation,
            RemoveOperation removeOperation,
            CopyOperation copyOperation,
            JoinPathsOperation joinPathsOperation,
            AddOperation addOperation,
            SubtractOperation subtractOperation,
            MultiplyOperation multiplyOperation,
            DivideOperation divideOperation,
            HashOperation hashOperation
    ) {
        this.operations = new EnumMap<>(TransformOperation.class);
        operations.put(TransformOperation.SET, setOperation);
        operations.put(TransformOperation.REMOVE, removeOperation);
        operations.put(TransformOperation.COPY, copyOperation);
        operations.put(TransformOperation.JOIN_PATHS, joinPathsOperation);
        operations.put(TransformOperation.ADD, addOperation);
        operations.put(TransformOperation.SUBTRACT, subtractOperation);
        operations.put(TransformOperation.MULTIPLY, multiplyOperation);
        operations.put(TransformOperation.DIVIDE, divideOperation);
        operations.put(TransformOperation.HASH, hashOperation);
    }

    public void executeTransformation(TransformationLoader.Transformation transformation, JsonNode payload, JsonNode result) {
        TransformOperation operationType = transformation.operation();

        TransformationOperation operation = operations.get(operationType);
        if (operation == null) {
            throw new TransformationException("Unsupported operation: " + operationType);
        }

        operation.execute(transformation, payload, result);
    }
}
