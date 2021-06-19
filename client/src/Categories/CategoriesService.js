const API_URL = process.env.REACT_APP_API_URL;

const checkStatus = (response) => {
    if(!response.ok) {
        throw Error(response.status);
    }
    return response;
}

const getCategories = async () => {

    const fetchData = {
        method: 'GET',
        mode: "cors",
        redirect: 'follow',
        credentials: 'include'
    };

    return fetch(`${API_URL}/categories`, fetchData)
        .then(response => checkStatus(response))
        .then(response => response.text())
        .then(response => JSON.parse(response))
        .catch(error => {
            return {success: false, status: error.message}
        });
}

export const categoriesService = { getCategories };