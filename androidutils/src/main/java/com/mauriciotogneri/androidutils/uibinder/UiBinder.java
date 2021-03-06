package com.mauriciotogneri.androidutils.uibinder;

import android.app.Activity;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.mauriciotogneri.androidutils.uibinder.annotations.BindView;
import com.mauriciotogneri.androidutils.uibinder.annotations.OnCheckedChanged;
import com.mauriciotogneri.androidutils.uibinder.annotations.OnClick;
import com.mauriciotogneri.androidutils.uibinder.annotations.OnItemClick;
import com.mauriciotogneri.androidutils.uibinder.annotations.OnItemLongClick;
import com.mauriciotogneri.androidutils.uibinder.annotations.OnItemSelected;
import com.mauriciotogneri.androidutils.uibinder.annotations.OnLongClick;
import com.mauriciotogneri.androidutils.uibinder.annotations.OnTextChanged;
import com.mauriciotogneri.androidutils.uibinder.containers.ActivityContainer;
import com.mauriciotogneri.androidutils.uibinder.containers.DialogContainer;
import com.mauriciotogneri.androidutils.uibinder.containers.UiContainer;
import com.mauriciotogneri.androidutils.uibinder.containers.ViewContainer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class UiBinder
{
    public UiBinder()
    {
    }

    public void bind(Activity activity)
    {
        bind(new ActivityContainer(activity), activity);
    }

    public void bind(Activity activity, Object target)
    {
        bind(new ActivityContainer(activity), target);
    }

    public void bind(View view, Object target)
    {
        bind(new ViewContainer(view), target);
    }

    public void bind(Dialog dialog, Object target)
    {
        bind(new DialogContainer(dialog), target);
    }

    private void bind(UiContainer uiContainer, Object target)
    {
        bindFields(uiContainer, target);
        bindMethods(uiContainer, target);
    }

    private void bindFields(UiContainer uiContainer, Object target)
    {
        for (Field field : target.getClass().getFields())
        {
            BindView bindView = field.getAnnotation(BindView.class);

            if (bindView != null)
            {
                if (Modifier.isPublic(field.getModifiers()))
                {
                    try
                    {
                        field.set(target, uiContainer.findViewById(bindView.value()));
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(String.format("Error binding view to field '%s'", field.getName()));
                    }
                }
                else
                {
                    throw new RuntimeException(String.format("The field '%s' must be public", field.getName()));
                }
            }
        }
    }

    private void bindMethods(UiContainer uiContainer, Object target)
    {
        for (Method method : target.getClass().getMethods())
        {
            CallableMethod callableMethod = new CallableMethod(method, target);

            bindOnClick(uiContainer, method, callableMethod);
            bindOnCheckedChanged(uiContainer, method, callableMethod);
            bindOnTextChanged(uiContainer, method, callableMethod);
            bindOnLongClick(uiContainer, method, callableMethod);
            bindOnItemClick(uiContainer, method, callableMethod);
            bindOnItemLongClick(uiContainer, method, callableMethod);
            bindOnItemSelectedClick(uiContainer, method, callableMethod);
        }
    }

    private void bindOnClick(UiContainer uiContainer, Method method, CallableMethod callableMethod)
    {
        OnClick onClick = method.getAnnotation(OnClick.class);

        if (onClick != null)
        {
            checkInvalidMethod(method);

            View view = uiContainer.findViewById(onClick.value());
            view.setOnClickListener(v -> callableMethod.call());
        }
    }

    private void bindOnLongClick(UiContainer uiContainer, Method method, CallableMethod callableMethod)
    {
        OnLongClick onLongClick = method.getAnnotation(OnLongClick.class);

        if (onLongClick != null)
        {
            checkInvalidMethod(method);

            View view = uiContainer.findViewById(onLongClick.value());
            view.setOnLongClickListener(v -> {
                callableMethod.call();

                return true;
            });
        }
    }

    private void bindOnCheckedChanged(UiContainer uiContainer, Method method, CallableMethod callableMethod)
    {
        OnCheckedChanged onCheckedChanged = method.getAnnotation(OnCheckedChanged.class);

        if (onCheckedChanged != null)
        {
            checkInvalidMethod(method);

            CompoundButton view = (CompoundButton) uiContainer.findViewById(onCheckedChanged.value());
            view.setOnCheckedChangeListener((compoundButton, changed) -> callableMethod.call(changed));
        }
    }

    private void bindOnTextChanged(UiContainer uiContainer, Method method, CallableMethod callableMethod)
    {
        OnTextChanged onTextChanged = method.getAnnotation(OnTextChanged.class);

        if (onTextChanged != null)
        {
            checkInvalidMethod(method);

            EditText view = (EditText) uiContainer.findViewById(onTextChanged.value());
            view.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after)
                {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int count, int after)
                {
                    callableMethod.call(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable)
                {
                }
            });
        }
    }

    private void bindOnItemClick(UiContainer uiContainer, Method method, CallableMethod callableMethod)
    {
        OnItemClick onItemClick = method.getAnnotation(OnItemClick.class);

        if (onItemClick != null)
        {
            checkInvalidMethod(method);

            AdapterView<?> view = (AdapterView<?>) uiContainer.findViewById(onItemClick.value());
            view.setOnItemClickListener((parent, view1, position, id) -> callableMethod.call(parent.getItemAtPosition(position)));
        }
    }

    private void bindOnItemLongClick(UiContainer uiContainer, Method method, CallableMethod callableMethod)
    {
        OnItemLongClick onItemLongClick = method.getAnnotation(OnItemLongClick.class);

        if (onItemLongClick != null)
        {
            checkInvalidMethod(method);

            AdapterView<?> view = (AdapterView<?>) uiContainer.findViewById(onItemLongClick.value());
            view.setOnItemLongClickListener((parent, view1, position, id) -> {
                callableMethod.call(parent.getItemAtPosition(position));

                return true;
            });
        }
    }

    private void bindOnItemSelectedClick(UiContainer uiContainer, Method method, CallableMethod callableMethod)
    {
        OnItemSelected onItemSelected = method.getAnnotation(OnItemSelected.class);

        if (onItemSelected != null)
        {
            checkInvalidMethod(method);

            AdapterView<?> view = (AdapterView<?>) uiContainer.findViewById(onItemSelected.value());
            view.setOnItemSelectedListener(new OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {
                    callableMethod.call(parent.getItemAtPosition(position));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent)
                {
                    callableMethod.call();
                }
            });
        }
    }

    private void checkInvalidMethod(Method method)
    {
        if (!Modifier.isPublic(method.getModifiers()))
        {
            throw new RuntimeException(String.format("The method '%s' must be public", method.getName()));
        }
    }
}