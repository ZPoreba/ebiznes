const API_URL = process.env.REACT_APP_API_URL;

const checkStatus = (response) => {
    console.log(response);
    if(!response.ok) {
        throw Error(response.status);
    }
    return response;
}

const register = async (firstName, lastName, email, password) => {

    let formdata = new FormData();
    formdata.append("firstName", firstName);
    formdata.append("lastName", lastName);
    formdata.append("email", email);
    formdata.append("password", password);

    const fetchData = {
        method: 'POST',
        mode: "cors",
        body: formdata,
        redirect: 'follow'
    };

    return fetch(`${API_URL}/signUp`, fetchData)
        .then(resp => {
            return resp;
        })
        .then(response => checkStatus(response))
        .then(response => response.text())
        .then(response => JSON.parse(response))
        .catch(error => {
            return {success: false, status: error.message}
        });
}

export const registrationService = { register };