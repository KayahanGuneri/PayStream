// Türkçe Özet:
// CreateForm bileşeninin alan bazlı hata mesajlarını (422 fieldErrors) doğru şekilde
// input altında gösterdiğini test eder.

import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CreateForm } from '../CreateForm';

describe('CreateForm validation rendering', () => {
  it('shows field errors under inputs when fieldErrors prop is provided', () => {
    const fieldErrors = {
      customerId: 'Customer id is required',
      currency: 'Currency is required',
    };
    const onSubmit = vi.fn();

    render(
      <CreateForm
        initialCustomerId="df6a5a49-23c0-4d8f-949c-9ae562f65f0d"
        pending={false}
        onSubmit={onSubmit}
        fieldErrors={fieldErrors}
      />
    );

    // Both error messages should be present
    expect(screen.getByText('Customer id is required')).toBeInTheDocument();
    expect(screen.getByText('Currency is required')).toBeInTheDocument();
  });

  it('renders general error banner when generalErrorMessage is provided', () => {
    const onSubmit = vi.fn();

    render(
      <CreateForm
        initialCustomerId="df6a5a49-23c0-4d8f-949c-9ae562f65f0d"
        pending={false}
        onSubmit={onSubmit}
        generalErrorMessage="Something went wrong"
      />
    );

    expect(screen.getByRole('alert')).toHaveTextContent('Something went wrong');
  });
});
