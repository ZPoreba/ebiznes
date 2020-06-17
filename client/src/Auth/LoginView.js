import React, {Component} from 'react';
import 'antd/dist/antd.css';
import { Form, Input, Button, Checkbox } from 'antd';
import { MailOutlined, LockOutlined, GooglePlusOutlined, FacebookOutlined } from '@ant-design/icons';
import { loginService } from "./LoginService";
import './style.css';
import {withRouter} from "react-router-dom";


class LoginView extends Component {

    constructor(props) {
        super(props);
    }

    google = () => {
        loginService.loginWithGoogle();
    }

    facebook = () => {
        loginService.loginWithFacebook();
    }

    onFinish = async values => {
        values.rememberMe = values.rememberMe ? values.rememberMe: false;
        loginService.login(values.email, values.password, values.rememberMe).then(resp => {
            loginService.setUser(resp);
            this.props.history.push('/search');
        })
    };

    render() {
        return (
            <div className="loginView">
                <Form
                    name="normal_login"
                    className="login-form"
                    initialValues={{
                        remember: true,
                    }}
                    onFinish={this.onFinish}
                >
                    <Form.Item
                        name="email"
                        rules={[
                            {
                                required: true,
                                message: 'Please input your E-mail!',
                            },
                        ]}
                    >
                        <Input prefix={<MailOutlined className="site-form-item-icon"/>} placeholder="E-mail"/>
                    </Form.Item>
                    <Form.Item
                        name="password"
                        rules={[
                            {
                                required: true,
                                message: 'Please input your Password!',
                            },
                        ]}
                    >
                        <Input.Password
                            prefix={<LockOutlined className="site-form-item-icon"/>}
                            placeholder="Password"
                        />
                    </Form.Item>
                    <Form.Item style={{float: 'left'}}>
                        <Form.Item name="rememberMe" valuePropName="checked" noStyle>
                            <Checkbox>Remember me</Checkbox>
                        </Form.Item>
                    </Form.Item>

                    <Form.Item style={{clear: "both"}}>
                        <Button type="primary" htmlType="submit" className="login-form-button">
                            Log in
                        </Button>
                        <p>
                            Not a member? <a href={`/register`}>Register now</a>
                        </p>
                        <p>
                            Or use existing account on one of the following services to log in:
                        </p>
                        <GooglePlusOutlined className={"google-icon"} onClick={() => this.google() } />
                        <FacebookOutlined className={"facebook-icon"} onClick={() => this.facebook() } />
                    </Form.Item>
                </Form>
            </div>
        );
    }
};

export default withRouter(LoginView);