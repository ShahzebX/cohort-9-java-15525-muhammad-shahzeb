export function getApiError(error) {
  if (error?.response?.data?.error) {
    return error.response.data.error
  }
  if (error?.response?.data?.message) {
    return error.response.data.message
  }
  if (error?.message) {
    return error.message
  }
  return 'Something went wrong. Please try again.'
}