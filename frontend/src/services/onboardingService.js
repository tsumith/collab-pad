import api from '../api';

export const onboardingService = {
  enrollCompany: async (companyName, selectedDepartments) => {
    const response = await api.post('/onboarding/enroll', {
      companyName,
      selectedDepartments
    });
    return response.data;
  }
};
