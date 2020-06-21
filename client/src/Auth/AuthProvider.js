import React, {Component} from 'react';
import {withRouter} from "react-router-dom";
import {loginService} from "./LoginService";
import { Spin } from 'antd';


const API_URL = process.env.REACT_APP_API_URL;

class AuthProvider extends Component {

    componentDidMount() {
        const fetchData = {
            method: 'GET',
            redirect: 'follow',
            credentials: 'include'
        };

        if(this.props.location.search.length > 0) {
            fetch(`${API_URL}/authenticate/${this.props.provider}${this.props.location.search}`, fetchData)
                .then(response => response.json())
                .then(resp => {
                    loginService.setUser(resp);
                    this.props.history.push('/search');
                });
        }
        else {
            this.props.history.push('/login');
        }
    }

    render() {
        return (<Spin>
                    <div style={{height: '100hv', width: '100wv'}}/>
                </Spin>);
    }
};

export default withRouter(AuthProvider);