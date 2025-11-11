import type { CreateCustomerResponse } from '../hooks/useCustomers';

export type CreateCustomerFormValues = {
  name: string;
  email: string;
  password: string;
};

export type CustomerDTO = CreateCustomerResponse;
