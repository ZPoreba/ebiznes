import React, {Component} from 'react';
import 'antd/dist/antd.css';
import { MailOutlined, LockOutlined, UserOutlined } from '@ant-design/icons';
import {
    Form,
    Input,
    Button
} from 'antd';
import { registrationService } from "./RegistrationService";
import {withRouter} from "react-router-dom";


class RegistrationView extends Component {

    onFinish = values => {
        registrationService.register(values.firstName, values.lastName, values.email, values.password).then(resp => {
            this.props.history.push('/login');
        })
    };

        render() {
            return (
                <div className={"registrationView"}>
                    <Form
                        name="register"
                        className={"register-form"}
                        onFinish={this.onFinish}
                    >
                        <Form.Item
                            name="firstName"
                            rules={[
                                {
                                    required: true,
                                    message: 'Please input your first name!',
                                    whitespace: true,
                                },
                            ]}
                        >
                            <Input prefix={<UserOutlined className="site-form-item-icon"/>} placeholder={'First Name'}/>
                        </Form.Item>

                        <Form.Item
                            name="lastName"
                            rules={[
                                {
                                    required: true,
                                    message: 'Please input your last name!',
                                    whitespace: true,
                                },
                            ]}
                        >
                            <Input prefix={<UserOutlined className="site-form-item-icon"/>} placeholder={'Last Name'}/>
                        </Form.Item>

                        <Form.Item
                            name="email"
                            rules={[
                                {
                                    type: 'email',
                                    message: 'The input is not valid E-mail!',
                                },
                                {
                                    required: true,
                                    message: 'Please input your E-mail!',
                                },
                            ]}
                        >
                            <Input prefix={<MailOutlined className="site-form-item-icon"/>} placeholder={"E-mail"}/>
                        </Form.Item>

                        <Form.Item
                            name="password"
                            rules={[
                                {
                                    required: true,
                                    message: 'Please input your password!',
                                },
                            ]}
                            hasFeedback
                        >
                            <Input.Password prefix={<LockOutlined className="site-form-item-icon"/>}
                                            placeholder={"Password"}/>
                        </Form.Item>

                        <Form.Item
                            name="confirm"
                            dependencies={['password']}
                            hasFeedback
                            rules={[
                                {
                                    required: true,
                                    message: 'Please confirm your password!',
                                },
                                ({getFieldValue}) => ({
                                    validator(rule, value) {
                                        if (!value || getFieldValue('password') === value) {
                                            return Promise.resolve();
                                        }

                                        return Promise.reject('The two passwords that you entered do not match!');
                                    },
                                }),
                            ]}
                        >
                            <Input.Password prefix={<LockOutlined className="site-form-item-icon"/>}
                                            placeholder={"Confirm Password"}/>
                        </Form.Item>

                        <Form.Item>
                            <Button type="primary" htmlType="submit" className="register-form-button">
                                Register
                            </Button>
                            <p>
                                Already a member? <a href={`/login`}>Log in now</a>
                            </p>
                        </Form.Item>
                    </Form>
                </div>
            );
        }
};

export default withRouter(RegistrationView);