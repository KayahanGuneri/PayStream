// Türkçe Özet:
// CreateForm üzerinden yapılan submit'te eksik currency için 422 field error'u gösterimi test edilir.

import React from 'react';
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CreateForm } from '../CreateForm';

describe('CreateForm validation', () => {
  it('shows field errors under inputs', async () => {
    const fieldErrors = { currency: 'Required' };
    const onSubmit = vi.fn();

    render(
      <CreateForm
        initialCustomerId="df6a5a49-23c0-4d8f-949c-9ae562f65f0d"
        pending={false}
        onSubmit={onSubmit}
        fieldErrors={fieldErrors}
      />
    );

    expect(screen.getByText('Required')).toBeInTheDocument();
  });
});
